package io.jacksoon.router.handler;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.BufferUtils;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.common.util.ResponseCheckResult;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.exception.BackendConnectionException;
import io.jacksoon.router.exception.context.RouterExceptionContext;
import io.jacksoon.router.pipeline.context.DetachedContexts;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.metric.ServiceMetricStore;
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

    private final ByteBuffer readBuffer = ByteBuffer.allocate(8 * 1024);

    private final CommonBlockingQueue<ProxyContext> requestQueue;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final HttpResponseCheck responseCheck;
    private final CommonBlockingQueue<ReRoutingContext> reRoutingQueue;
    private final ExceptionDispatcher exceptionDispatcher;
    private final ServiceMetricStore serviceMetricStore;
    @Setter
    private BackendConnectionPool connectionPool;
    private volatile ProxyContext currentWriteContext;
    private final Object stateLock = new Object();
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private final AtomicInteger pendingCount = new AtomicInteger();
    private final String serviceName;
    private final ByteBuffer idleReadBuffer = ByteBuffer.allocate(1);
    private volatile long idleSince;
    private volatile long connectStartedAt;
    private volatile long requestStartedAt;
    private volatile boolean closed;
    private volatile boolean isReconnection;

    public BackendIOHandler(String serviceName, Selector selector, SocketChannel socketChannel, CommonBlockingQueue<ProxyContext> requestQueue, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck, CommonBlockingQueue<ReRoutingContext> reRoutingQueue, ExceptionDispatcher exceptionDispatcher, ServiceMetricStore serviceMetricStore) {
        super(selector, socketChannel);
        this.requestQueue = requestQueue;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
        this.serviceName = serviceName;
        this.reRoutingQueue = reRoutingQueue;
        this.exceptionDispatcher = exceptionDispatcher;
        this.serviceMetricStore = serviceMetricStore;
        this.idleSince = System.currentTimeMillis();
        this.connectStartedAt = this.idleSince;
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
        } catch (IOException e) {
            failAndClose(new BackendConnectionException("Backend IO failed. serviceName=" + serviceName, e));
        } catch (RuntimeException e) {
            BackendConnectionException failure = e instanceof BackendConnectionException backendException
                    ? backendException
                    : new BackendConnectionException(
                    "Backend handler failed. serviceName=" + serviceName, e);

            failAndClose(failure);
        }
    }

    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }
        isReconnection = false;
        connectStartedAt = 0L;
        if (currentWriteContext != null || !requestQueue.isEmpty()) {
            setInterestOpsNoWakeup(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            return;
        }

        setInterestOpsNoWakeup(SelectionKey.OP_READ);
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
                    setInterestOpsNoWakeup(SelectionKey.OP_READ);
                    return;
                }
                requestStartedAt = System.currentTimeMillis();
            }
        }
        socketChannel.write(currentWriteContext.requestBuffer);
        if (currentWriteContext.requestBuffer.hasRemaining()) {
            setInterestOpsNoWakeup(SelectionKey.OP_WRITE);
            return;
        }
        setInterestOpsNoWakeup(SelectionKey.OP_READ);
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
            readIdleConnection();
            return;
        }
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

    private void readIdleConnection() throws IOException {
        idleReadBuffer.clear();
        int read = socketChannel.read(idleReadBuffer);
        if (read == -1) {
            failAndClose(new IOException("Backend closed idle connection. serviceName=" + serviceName));
            return;
        }
        if (read > 0) {
            failAndClose(new IOException("Unexpected backend data while idle. serviceName=" + serviceName));
        }
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
                    return true;
                }
                if (currentWriteContext == null) {
                    int currentOps = selectionKey.interestOps();
                    if ((currentOps & SelectionKey.OP_WRITE) == 0) {
                        setInterestOps(currentOps | SelectionKey.OP_WRITE);
                    }
                    return true;
                }
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
            throw new BackendConnectionException("Invalid backend response length: " + result.responseLength());
        }
        responseBuffer.limit(result.responseLength());
        if (result.connectionClose() && !result.closeDelimited()) {
            responseBuffer = removeConnectionHeader(responseBuffer);
        }
        boolean reusable = !backendClosed
                && !result.connectionClose()
                && !result.closeDelimited();
        boolean necessaryReconnection = result.connectionClose();
        synchronized (stateLock) {
            if (closed || currentWriteContext != proxyContext) {
                return;
            }
            currentWriteContext = null;
            requestStartedAt = 0L;
            if (necessaryReconnection) {
                isReconnection = true;
                setInterestOpsNoWakeup(0);
            } else if (reusable) {
                if (requestQueue.isEmpty()) {
                    setInterestOpsNoWakeup(SelectionKey.OP_READ);
                } else {
                    setInterestOpsNoWakeup(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }
            } else {
                setInterestOpsNoWakeup(0);
            }
        }
        SocketChannel clientChannel = null;
        if (proxyContext.clientKey != null && proxyContext.clientKey.channel() instanceof SocketChannel channel) {
            clientChannel = channel;
        }
        RouterPipelineContext context = new RouterPipelineContext(
                clientChannel,
                "backend-response",
                responseBuffer,
                responseBuffer.remaining(),
                proxyContext.clientKey,
                proxyContext.current
        );
        context.setCloseAfterWrite(result.closeDelimited());
        routerPipelineQueue.put(context);
        serviceMetricStore.success(serviceName);
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
            this.socketChannel.connect(new InetSocketAddress(connectionPool.getEndpoint().getHost(), connectionPool.getEndpoint().getPort()));
            this.selectionKey = this.socketChannel.register(selector, SelectionKey.OP_CONNECT, this);
        } catch (IOException e) {
            failAndClose(new BackendConnectionException("Backend reconnection failed. serviceName=" + serviceName, e));
        }
    }

    @Override
    public void close() {
        failAndClose(new IOException("Backend connection closed"));
    }

    public void closeByPool() {
        failAndClose(new IOException("Backend connection closed by pool"));
    }

    private void failAndClose(Throwable cause) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        DetachedContexts detached = detachAssignedContexts();
        closeTransport();
        failCurrent(detached.current(), cause);
        for (ProxyContext pending : detached.pending()) {
            reRoutingRequest(pending);
        }
    }

    private DetachedContexts detachAssignedContexts() {
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
        return new DetachedContexts(current, failedContexts);
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

    private void failCurrent(ProxyContext context, Throwable cause) {
        if (context == null) {
            return;
        }
        serviceMetricStore.failure(serviceName);
        BackendConnectionException failure = cause instanceof BackendConnectionException backendException
                ? backendException
                : new BackendConnectionException("Backend request failed. serviceName=" + serviceName, cause);
        exceptionDispatcher.dispatch(RouterExceptionContext.of(context), failure);
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