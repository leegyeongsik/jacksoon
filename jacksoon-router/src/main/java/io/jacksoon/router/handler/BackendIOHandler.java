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
    // 여기서는 근데 write read 사이클을 write로 보냈으면 끝날때까지 read 유지로 하긴해야됨 왜냐면 막 write를 보냈을때 어떤게 어떤 리든지 모름
    // 일단 풀 send write로 바꿈  그리고 버릴떄 대기하고있는애들 보상처리해주고
    // 그리고 reduce워커랑 핸들러랑 충돌날수있음 리액터는 얘가 일을 하다가 reduce워커가 이 핸들러를 없애는거임 없앨때 close도 할건데 그러면 찐빠나는거임
    // 그리고 그냥 비동기로 대기하고있는애들 보내버리고 온순서대로 기록한다음에 그거 채우고 반환하면 순서도 지키고 반환도 빨리하고 좀더 효율적이지않을까 이거는 식별이안되서 그니까 일단 다 보내는데 a클 b클 c클 이 큐에있었고 그걸 다 보내서 반환받았다고 해보자 그랬을때 제일먼저온게 peek이 보장되지않음  1.1는 좀 제한될듯 그래서 풀을 이용해서 하나씩 처리하자 그리고 풀로 병렬로 처리하자
    // 클라이언트한테 반환도 그냥 다 모았다가 read에서 다 모아서 클라이언트 write로 보내지말고 온대로 보내버리는거지 부분부분 그리고 다 왔는지만 체크하고
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

    private void write() throws IOException, InterruptedException {
        synchronized(stateLock){
            if(closed){
                return;
            }

            if (currentWriteContext == null) {
                currentWriteContext = requestQueue.take();
                if (currentWriteContext == null) {
                    setInterestOps(0);
                    return;
                }
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
        synchronized (stateLock){
            if(closed){
                return;
            }
        }
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
                } else if (currentWriteContext == null) {
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
        if (!reusable) {
            failAndClose(new IOException("Backend connection is not reusable"));
            return;
        }
        if (connectionPool != null) {
            connectionPool.complete(this);
        } else {
            decreasePending();
        }
    }

    @Override
    public void close() {
        synchronized (stateLock) {
            if (closed) {
                return;
            }
            currentWriteContext = null;
            pendingCount.set(0);
            terminated.set(true);
            closed = true;
        }
        closeTransport();
    }
    public void closeByPool() {
        synchronized (stateLock) {
            if (closed) {
                return;
            }
            currentWriteContext = null;
            pendingCount.set(0);
            terminated.set(true);
            closed = true;
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