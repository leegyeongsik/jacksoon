package io.jacksoon.router.handle;


import io.jacksoon.router.help.HttpResponseCheck;
import io.jacksoon.router.help.ResponseCheckResult;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.connection.RequestBackendQueue;
import io.jacksoon.router.worker.connection.ResponseBackendQueue;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class BackendIOHandler implements Handler {
    private final SocketChannel socketChannel;
    @Getter
    private final SelectionKey selectionKey;
    private final RequestBackendQueue requestQueue;
    private final ResponseBackendQueue responseQueue;
    private final HttpResponseCheck responseCheck;
    final RequestPipelineQueue requestPipelineQueue;
    private ProxyContext currentWriteContext;

    public BackendIOHandler(Selector selector, SocketChannel socketChannel, ResponseBackendQueue responseBackendQueue, RequestBackendQueue requestBackendQueue, RequestPipelineQueue requestPipelineQueue, HttpResponseCheck responseCheck) {
        this.socketChannel = socketChannel;
        this.requestQueue = requestBackendQueue;
        this.responseQueue = responseBackendQueue;
        this.requestPipelineQueue = requestPipelineQueue;
        this.responseCheck = responseCheck;
        try {
            selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
        selectionKey.attach(this);

        selector.wakeup();
    }

    @Override
    public void handle() {
        try {
            if (selectionKey.isConnectable()) {
                connect();
            } else if (selectionKey.isReadable()) {
                read();
            } else if (selectionKey.isWritable()) {
                write();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            ResponseCheckResult result = responseCheck.check(readBuffer, proxyContext.responseBuffer);
            if (!result.complete()) {
                return;
            }
            completeBackendResponse(proxyContext, result, false);

        } catch (IOException e) {
            close();
        }
    }

    private void connect() throws IOException {
        if (socketChannel.finishConnect()) {
            if (!requestQueue.isEmpty()) {
                selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
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

    private void completeBackendResponse(ProxyContext proxyContext, ResponseCheckResult result, boolean backendClosed) throws InterruptedException {
        responseQueue.poll();
        ByteBuffer responseBuffer = proxyContext.responseBuffer;
        responseBuffer.flip();
        responseBuffer.limit(result.responseLength());

        PipelineContext context =
                new PipelineContext(
                        socketChannel,
                        "backend-response",
                        responseBuffer,
                        result.responseLength(),
                        proxyContext.bufferContext,
                        proxyContext.clientKey
                );

        requestPipelineQueue.put(context);

        if (backendClosed) {
            close();
            return;
        }

        if (!requestQueue.isEmpty()) {
            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else {
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    private void close() {
        try {
            if (selectionKey != null) {
                selectionKey.cancel();
            }
        } catch (Exception ignored) {
        }
        try {
            socketChannel.close();
        } catch (IOException ignored) {
        }
    }
}
