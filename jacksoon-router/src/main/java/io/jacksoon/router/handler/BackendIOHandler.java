package io.jacksoon.router.handler;

import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.BufferUtils;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.common.util.ResponseCheckResult;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.dto.ServiceRequest;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BackendIOHandler extends NioConnectionHandler {
    private static final long CONNECT_TIMEOUT_MILLIS = 5_000L;
    private static final long RESPONSE_TIMEOUT_MILLIS = 30_000L;
    private final CommonBlockingQueue<ProxyContext> requestQueue;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final CommonBlockingQueue<ServiceRequest> serviceRequestQueue;
    private final HttpResponseCheck responseCheck;
    private final CommonBlockingQueue<ReRoutingContext> reRoutingQueue;
    @Setter
    private BackendConnectionPool connectionPool;
    private volatile ProxyContext currentWriteContext;
    private final Object stateLock = new Object();
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final AtomicInteger pendingCount = new AtomicInteger();
    private final String serviceName;
    private volatile long idleSince;
    private volatile long connectStartedAt;
    private volatile long requestStartedAt;
    private volatile boolean closed;
    private volatile boolean isReconnection;

    public BackendIOHandler(String serviceName, Selector selector, SocketChannel socketChannel, CommonBlockingQueue<ProxyContext> requestQueue, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck, CommonBlockingQueue<ServiceRequest> serviceRequestQueue, CommonBlockingQueue<ReRoutingContext> reRoutingQueue) {
        super(selector, socketChannel);
        this.requestQueue = requestQueue;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
        this.serviceName = serviceName;
        this.reRoutingQueue = reRoutingQueue;
        this.idleSince = System.currentTimeMillis();
        this.connectStartedAt = this.idleSince;
        this.serviceRequestQueue = serviceRequestQueue;
        this.isReconnection = false;
        setInterestOps(SelectionKey.OP_CONNECT);
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
        return !closed
                && selectionKey != null
                && selectionKey.isValid()
                && socketChannel != null
                && socketChannel.isOpen();
    }

    public boolean removable(long now, long idleTimeoutMillis) {
        return pendingCount.get() == 0
                && currentWriteContext == null
                && requestQueue.isEmpty()
                && now - idleSince >= idleTimeoutMillis;
    }

    public void checkTimeout(long now) {
        if (closed) {
            return;
        }

        if (!socketChannel.isConnected()) {
            if (now - connectStartedAt >= CONNECT_TIMEOUT_MILLIS) {
                failAndClose(new IOException("Backend connect timeout"));
            }
            return;
        }

        long startedAt = requestStartedAt;
        if (startedAt > 0 && currentWriteContext != null && now - startedAt >= RESPONSE_TIMEOUT_MILLIS) {
            failAndClose(new IOException("Backend response timeout"));
        }
    }

    @Override
    public void handle() {
        try {
            if (closed) {
                return;
            }

            if (selectionKey == null || !selectionKey.isValid()) {
                failAndClose(new IOException("Backend selection key is invalid"));
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
        } catch (IOException | RuntimeException e) {
            failAndClose(e);
        }
    }

    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }
        isReconnection = false;
        connectStartedAt = 0L;
        if (currentWriteContext != null || !requestQueue.isEmpty()) {
            setInterestOps(SelectionKey.OP_WRITE);
        } else {
            setInterestOps(0);
        }
    }

    private void write() throws IOException {
        synchronized (stateLock) {
            if (isReconnection) {
                return;
            }
            if (closed) {
                return;
            }
            if (currentWriteContext == null) {
                currentWriteContext = requestQueue.pollNow();
                if (currentWriteContext == null) {
                    setInterestOps(0);
                    return;
                }
                requestStartedAt = System.currentTimeMillis();
            }
        }
        socketChannel.write(currentWriteContext.requestBuffer);
        if (currentWriteContext.requestBuffer.hasRemaining()) {
            setInterestOps(SelectionKey.OP_WRITE);
            return;
        }
        setInterestOps(SelectionKey.OP_READ);
    }

    private void read() throws IOException {
        synchronized (stateLock) {
            if (isReconnection) {
                return;
            }
            if (closed) {
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
        proxyContext.responseBuffer = BufferUtils.ensureResponseCapacity(proxyContext.responseBuffer, readBuffer.remaining());
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
                requestQueue.put(context); // 상관없어 어차피 OP_WRITE해도 write에서 막힘 , OP_CONNECT은 어차피 커넥션이 중일테니까 상태바꿔도됨
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
        if (result.connectionClose() && !result.closeDelimited()) {
            responseBuffer = removeConnectionHeader(responseBuffer);
        }
        RouterPipelineContext context = new RouterPipelineContext(
                socketChannel,
                "backend-response",
                responseBuffer,
                responseBuffer.remaining(),
                proxyContext.clientKey,
                proxyContext.current
        );
        context.setCloseAfterWrite(result.closeDelimited());
        routerPipelineQueue.put(context);
        serviceRequestQueue.put(new ServiceRequest(serviceName, true));
        boolean reusable = !backendClosed
                && !result.connectionClose()
                && !result.closeDelimited();
        boolean necessaryReconnection = result.connectionClose();
        synchronized (stateLock) {
            currentWriteContext = null;
            requestStartedAt = 0L;
            if (necessaryReconnection) {
                isReconnection = true;
                setInterestOps(0);
            } else if (reusable) {
                if (requestQueue.isEmpty()) {
                    setInterestOps(0);
                } else {
                    setInterestOps(SelectionKey.OP_WRITE);
                }
            } else {
                setInterestOps(0);
            }
        }

        if (connectionPool != null) {
            connectionPool.complete(this);
        } else {
            decreasePending();
        }
        if (necessaryReconnection) {
            reconnection();
            return;
        }
        if (!reusable) {
            failAndClose(new IOException("Backend connection is not reusable"));
        }
    }

    private void reconnection() {
        super.close();
        try {
            this.socketChannel = SocketChannel.open();
            this.socketChannel.configureBlocking(false);
            this.connectStartedAt = System.currentTimeMillis();
            this.socketChannel.connect(new InetSocketAddress(connectionPool.endpoint().getHost(), connectionPool.endpoint().getPort()));
            this.selectionKey = this.socketChannel.register(selector, SelectionKey.OP_CONNECT, this);
            selector.wakeup();
        } catch (IOException e) {
            failAndClose(new IOException("Backend reconnection fail", e));
        }
    }

    @Override
    public void close() {
        failAndClose(new IOException("Backend connection closed"));
    }

    public void closeByPool() {
        failAndClose(new IOException("Backend connection closed by pool"));
    }
    public int load() {
        return pendingCount.get();
    }
    private void failAndClose(Throwable cause) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        List<ProxyContext> pendingContexts = detachAssignedContexts();
        closeTransport();
        for (ProxyContext pending : pendingContexts) {
            reRoutingRequest(pending);
        }
    }

    private List<ProxyContext> detachAssignedContexts() {
        List<ProxyContext> failedContexts = new ArrayList<>();
        ProxyContext current;
        synchronized (stateLock) {
            closed = true;
            requestStartedAt = 0L;
            current = currentWriteContext;
            currentWriteContext = null;

            drainRequestQueue(failedContexts);
            pendingCount.set(0);
        }
        if (current != null) {
            failCurrent(current);
        }
        return failedContexts;
    }

    private void drainRequestQueue(List<ProxyContext> contexts) {
        while (true) {
            ProxyContext context = requestQueue.pollNow();
            if (context == null) {
                return;
            }
            contexts.add(context);
        }
    }

    private void reRoutingRequest(ProxyContext context) {
        if (context == null) {
            return;
        }
        reRoutingQueue.put(new ReRoutingContext(context, serviceName));
    }

    private void failCurrent(ProxyContext context) {
        serviceRequestQueue.put(new ServiceRequest(serviceName, false));
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

    private ByteBuffer removeConnectionHeader(ByteBuffer source) {
        ByteBuffer view = source.asReadOnlyBuffer();
        byte[] bytes = new byte[view.remaining()];
        view.get(bytes);
        int headerEnd = findHeaderEnd(bytes);
        if (headerEnd == -1) {
            return source;
        }
        String header = new String(bytes, 0, headerEnd, StandardCharsets.ISO_8859_1);
        String[] lines = header.split("\r\n");
        StringBuilder newHeader = new StringBuilder();

        newHeader.append(lines[0]).append("\r\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            int colon = line.indexOf(':');
            if (colon <= 0) {
                newHeader.append(line).append("\r\n");
                continue;
            }
            String name = line.substring(0, colon).trim();
            if (name.equalsIgnoreCase("Connection") || name.equalsIgnoreCase("Keep-Alive")) {
                continue;
            }
            newHeader.append(line).append("\r\n");
        }
        newHeader.append("\r\n");
        byte[] headerBytes = newHeader.toString().getBytes(StandardCharsets.ISO_8859_1);
        int bodyStart = headerEnd + 4;
        int bodyLength = bytes.length - bodyStart;
        ByteBuffer result = ByteBuffer.allocate(headerBytes.length + bodyLength);
        result.put(headerBytes);
        result.put(bytes, bodyStart, bodyLength);
        result.flip();

        return result;
    }

    private int findHeaderEnd(byte[] bytes) {
        for (int i = 0; i <= bytes.length - 4; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n' && bytes[i + 2] == '\r' && bytes[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
}