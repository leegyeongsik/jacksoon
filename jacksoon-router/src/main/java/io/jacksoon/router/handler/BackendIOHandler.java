package io.jacksoon.router.handler;

import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.common.util.ResponseCheckResult;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BackendIOHandler extends NioConnectionHandler {

    private final CommonBlockingQueue<ProxyContext> requestQueue;
    private final CommonBlockingQueue<ProxyContext> responseQueue;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final HttpResponseCheck responseCheck;

    private final long id;

    private BackendConnectionPool connectionPool;

    private ProxyContext currentWriteContext;

    private int pendingCount;
    private long idleSince;

    private volatile boolean closed;

    public BackendIOHandler(long id, Selector selector, SocketChannel socketChannel, CommonBlockingQueue<ProxyContext> requestQueue, CommonBlockingQueue<ProxyContext> responseQueue, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck) {
        super(selector, socketChannel, SelectionKey.OP_CONNECT);
        this.id = id;
        this.requestQueue = requestQueue;
        this.responseQueue = responseQueue;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
        this.idleSince = System.currentTimeMillis();
    }

    public void setConnectionPool(BackendConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public long id() {
        return id;
    }

    public int pendingCount() {
        return pendingCount;
    }

    public void increasePending() {
        pendingCount++;
    }

    public void decreasePending() {
        if (pendingCount > 0) {
            pendingCount--;
        }

        if (pendingCount == 0) {
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
        return pendingCount == 0
                && currentWriteContext == null
                && requestQueue.isEmpty()
                && responseQueue.isEmpty()
                && now - idleSince >= idleTimeoutMillis;
    }

    @Override
    public void handle() {
        try {
            if (closed || selectionKey == null || !selectionKey.isValid()) {
                close();
                return;
            }

            if (selectionKey.isConnectable()) {
                connect();
            }

            if (selectionKey.isReadable()) {
                read();
            }

            if (selectionKey.isWritable()) {
                write();
            }
        } catch (IOException | InterruptedException e) {
            close();
        }
    }

    public void read() throws InterruptedException {
        ProxyContext proxyContext = responseQueue.peek();

        if (proxyContext == null) {
            return;
        }

        ByteBuffer readBuffer = proxyContext.readBuffer;
        readBuffer.clear();

        try {
            int read = socketChannel.read(readBuffer);

            if (read == -1) {
                ResponseCheckResult result = responseCheck.eof(proxyContext.responseBuffer);

                if (!result.complete()) {
                    close();
                    return;
                }

                completeBackendResponse(proxyContext, result, true);
                return;
            }

            if (read == 0) {
                return;
            }

            readBuffer.flip();

            ResponseCheckResult result =
                    responseCheck.check(readBuffer, proxyContext.responseBuffer);

            if (!result.complete()) {
                return;
            }

            completeBackendResponse(proxyContext, result, false);

        } catch (IOException e) {
            close();
        }
    }

    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }

        if (!requestQueue.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    private void write() throws IOException, InterruptedException {
        if (currentWriteContext == null) {
            if (!responseQueue.isEmpty()) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                return;
            }
            currentWriteContext = requestQueue.poll();
            if (currentWriteContext == null) {
                selectionKey.interestOps(SelectionKey.OP_READ);
                return;
            }
        }

        int written = socketChannel.write(currentWriteContext.requestBuffer);

        if (written == 0) {
            return;
        }

        if (currentWriteContext.requestBuffer.hasRemaining()) {
            return;
        }

        responseQueue.put(currentWriteContext);
        currentWriteContext = null;

        selectionKey.interestOps(SelectionKey.OP_READ);
    }

    public boolean send(ProxyContext context) {
        if (closed || selectionKey == null || !selectionKey.isValid()) {
            close();
            return false;
        }

        if (socketChannel == null || !socketChannel.isOpen()) {
            close();
            return false;
        }

        try {
            requestQueue.put(context);

            if (!socketChannel.isConnected()) {
                selectionKey.interestOps(SelectionKey.OP_CONNECT);
                selector.wakeup();
                return true;
            }

            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            selector.wakeup();
            return true;

        } catch (RuntimeException e) {
            close();
            return false;
        }
    }

    private void completeBackendResponse(ProxyContext proxyContext, ResponseCheckResult result, boolean backendClosed) {

        responseQueue.poll();

        ByteBuffer responseBuffer = proxyContext.responseBuffer;
        responseBuffer.flip();
        responseBuffer.limit(result.responseLength());

        RouterPipelineContext context =
                new RouterPipelineContext(
                        socketChannel,
                        "backend-response",
                        responseBuffer,
                        result.responseLength(),
                        proxyContext.bufferContext,
                        proxyContext.clientKey
                );

        routerPipelineQueue.put(context);

        if (connectionPool != null) {
            connectionPool.complete(this);
        }

        if (backendClosed) {
            close();
            return;
        }

        if (closed || selectionKey == null || !selectionKey.isValid()) {
            return;
        }

        if (!requestQueue.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            super.close();
        } finally {
            if (connectionPool != null) {
                connectionPool.removeClosed(this);
            }
        }
    }

    public void closeByPool() {
        if (closed) {
            return;
        }

        closed = true;
        super.close();
    }

    public int load() {
        return pendingCount;
    }
}

