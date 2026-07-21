package io.jacksoon.router.handler;

import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.common.util.ResponseCheckResult;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.produce.dto.ServiceRequest;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendIOHandler extends NioConnectionHandler {

    private final CommonBlockingQueue<ProxyContext> requestQueue;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final CommonBlockingQueue<ServiceRequest> serviceRequestQueue;
    private final HttpResponseCheck responseCheck;
    @Setter
    private BackendConnectionPool connectionPool;
    private ProxyContext currentWriteContext;
    private final Object stateLock = new Object();
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final AtomicInteger pendingCount = new AtomicInteger();
    private long idleSince;
    private volatile boolean closed;
    private final String serviceName;

    public BackendIOHandler(String serviceName, Selector selector, SocketChannel socketChannel, CommonBlockingQueue<ProxyContext> requestQueue, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck, CommonBlockingQueue<ServiceRequest> serviceRequestQueue) {
        super(selector, socketChannel, SelectionKey.OP_CONNECT);
        this.requestQueue = requestQueue;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
        this.serviceName = serviceName;
        this.idleSince = System.currentTimeMillis();
        this.serviceRequestQueue = serviceRequestQueue;
    }

    public void increasePending() {
        pendingCount.incrementAndGet();
    }

    public void decreasePending() {
        int next = pendingCount.updateAndGet(current -> Math.max(0, current - 1));
        if (next == 0) {
            idleSince = System.currentTimeMillis();
        }
    }

    public boolean isAlive() {
        return !closed && selectionKey != null && selectionKey.isValid() && socketChannel != null && socketChannel.isOpen();
    }

    public boolean removable(long now, long idleTimeoutMillis) {
        return pendingCount.get() == 0 && currentWriteContext == null && requestQueue.isEmpty() && now - idleSince >= idleTimeoutMillis;
    }

    @Override
    public void handle() {
        try {
            if (closed) {
                return;
            }

            if (selectionKey == null || !selectionKey.isValid()) {
                failAndClose(
                        new IOException("Backend selection key is invalid")
                );
                return;
            }

            if (selectionKey.isConnectable()) {
                connect();
            }

            if (closed || !selectionKey.isValid()) {
                return;
            }

            if (selectionKey.isReadable()) {
                read();
            }

            if (closed || !selectionKey.isValid()) {
                return;
            }

            if (selectionKey.isWritable()) {
                write();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failAndClose(e);

        } catch (IOException | RuntimeException e) {
            failAndClose(e);
        }
    }

    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }

        if (currentWriteContext != null || !requestQueue.isEmpty()) {
            setInterestOps(SelectionKey.OP_WRITE);
        } else {
            setInterestOps(0);
        }
    }

    private void write() throws IOException, InterruptedException { // 어차피 router가 찾아서 거따가 write로 상태 바꾸는건데
        //currentWriteContext가 안비워졌으면 계속 read로 돌려서 걔부터 빠져 나가게끔
        if (currentWriteContext == null) {
            currentWriteContext = requestQueue.poll();
            if (currentWriteContext == null) {
                setInterestOps(0);
                return;
            }
        }
        socketChannel.write(currentWriteContext.requestBuffer);
        if (currentWriteContext.requestBuffer.hasRemaining()) {
            setInterestOps(SelectionKey.OP_WRITE);
            return;
        }
        setInterestOps(SelectionKey.OP_READ);
    }

    private void read() throws IOException, InterruptedException {
        ProxyContext proxyContext = currentWriteContext;
        if (proxyContext == null) {
            setInterestOps(0);
            return;
        }
        ByteBuffer readBuffer = proxyContext.readBuffer;
        readBuffer.clear();
        int read = socketChannel.read(readBuffer);
        if (read == -1) {
            ResponseCheckResult result = responseCheck.eof(proxyContext.responseBuffer);
            if (!result.complete()) {
                throw new IOException("Backend closed before response completed");
            }
            completeBackendResponse(proxyContext, result, true);
            return;
        }
        if (read == 0) {
            return;
        }
        readBuffer.flip();
        ResponseCheckResult result = responseCheck.check(readBuffer, proxyContext.responseBuffer);
        if (!result.complete()) {
            return;
        }
        completeBackendResponse(proxyContext, result, false);
    }

    public boolean send(ProxyContext context) {
        synchronized (stateLock) {
            if (closed || selectionKey == null || !selectionKey.isValid()) {
                return false;
            }
            if (socketChannel == null || !socketChannel.isOpen()) {
                return false;
            }
            try {
                requestQueue.put(context);
                if (!socketChannel.isConnected()) {
                    setInterestOps(SelectionKey.OP_CONNECT);
                } else if (currentWriteContext == null) {     // 여기서 null이면 걔가 보내지고 받아질떄까지가 한 사이클임 그래서 null일때 write
                    setInterestOps(SelectionKey.OP_WRITE);
                }
                selector.wakeup();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    private void completeBackendResponse(ProxyContext proxyContext, ResponseCheckResult result, boolean backendClosed) {
        ByteBuffer responseBuffer = proxyContext.responseBuffer;
        responseBuffer.flip();
        if (result.responseLength() > responseBuffer.limit()) {
            throw new IllegalStateException("Invalid response length: " + result.responseLength());
        }
        responseBuffer.limit(result.responseLength());
        RouterPipelineContext context = new RouterPipelineContext(socketChannel, "backend-response", responseBuffer, result.responseLength(), proxyContext.bufferContext, proxyContext.clientKey);
        routerPipelineQueue.put(context);
        serviceRequestQueue.put(new ServiceRequest(serviceName, true));
        boolean reusable = !backendClosed && !result.connectionClose() && !result.closeDelimited();
        synchronized (stateLock) {
            currentWriteContext = null;
            if (reusable) {
                if (requestQueue.isEmpty()) {
                    setInterestOps(0);
                } else {
                    setInterestOps(SelectionKey.OP_WRITE);
                }
            }
        }
        if (connectionPool != null) {
            connectionPool.complete(this);
        } else {
            decreasePending();
        }
        if (!reusable) {
            failQueuedAndClose(new IOException("Backend connection is not reusable"));
        }
        // 0이 send의 write를 덮어도 그 시점에 put을 한 상태니까 밑에 empty가 아니라 write로 상태가 바뀜 근데 그냥 락거는게 맘편할듯
        //        currentWriteContext = null;  // 이거 근데 send스레드랑 read스레드가 동시에 들어왔을때 이벤트가 무시될 가능성있음
        //        if (!requestQueue.isEmpty()) {
        //            setInterestOps(SelectionKey.OP_WRITE);
        //        } else {
        //            setInterestOps(0);
        //        }
    }

    @Override
    public void close() {
        // 했을때 파이프라인에 넘김 실패요청이라던가
        // 닫을때 request큐에 쌓여있는거 처리
        // 종료원인 구분해서 정상적이면 ok 아니면 current 랑 request남아있는거 다른곳으로 보냄
        synchronized (stateLock) {
            if (closed) {
                return;
            }
            closed = true;
            terminated.set(true);
            currentWriteContext = null;
            pendingCount.set(0);
        }
        closeTransport();
    }

    public void closeByPool() {
        synchronized (stateLock) {
            if (closed) {
                return;
            }

            closed = true;
            terminated.set(true);
            currentWriteContext = null;
            pendingCount.set(0);
        }
        super.close();
    }

    public int load() {
        return pendingCount.get();
    }

    private void failAndClose(Throwable cause) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        List<ProxyContext> failedContexts = detachAssignedContexts();
        closeTransport();
        for (ProxyContext failedContext : failedContexts) {
            failProxyContext(failedContext, cause);
        }
    }

    private void failQueuedAndClose(Throwable cause) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        List<ProxyContext> failedContexts = detachQueuedContexts();
        closeTransport();
        for (ProxyContext failedContext : failedContexts) {
            failProxyContext(failedContext, cause);
        }
    }

    private List<ProxyContext> detachAssignedContexts() {
        List<ProxyContext> failedContexts = new ArrayList<>();
        synchronized (stateLock) {
            closed = true;
            if (currentWriteContext != null) {
                failedContexts.add(currentWriteContext);
                currentWriteContext = null;
            }
            drainRequestQueue(failedContexts);
            pendingCount.set(0);
        }

        return failedContexts;
    }

    private List<ProxyContext> detachQueuedContexts() {
        List<ProxyContext> failedContexts = new ArrayList<>();
        synchronized (stateLock) {
            closed = true;
            drainRequestQueue(failedContexts);
            pendingCount.set(0);
        }

        return failedContexts;
    }

    private void drainRequestQueue(List<ProxyContext> contexts) {
        while (!requestQueue.isEmpty()) {
            try {
                ProxyContext context = requestQueue.poll();
                if (context == null) {
                    return;
                }
                contexts.add(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void failProxyContext(ProxyContext context, Throwable cause) {
        if (context == null) {
            return;
        }
        serviceRequestQueue.put(new ServiceRequest(serviceName, false));
        try {
            if (context.clientKey != null) {
                context.clientKey.cancel();
                context.clientKey.channel().close();
            }
        } catch (IOException ignored) {
        }

        System.err.println(
                "Backend request failed"
                        + ", service=" + serviceName
                        + ", cause="
                        + cause.getClass().getSimpleName()
                        + ": "
                        + cause.getMessage()
        );
    }

    private void closeTransport() {
        try {
            super.close();
        } finally {
            if (connectionPool != null) {
                connectionPool.removeClosed(this);
            }
        }
    }
}