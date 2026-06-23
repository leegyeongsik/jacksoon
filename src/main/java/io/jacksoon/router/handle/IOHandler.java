package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.help.BufferUtils;
import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.help.RequestCheckResult;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class IOHandler implements Handler {
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer readBuffer = ByteBuffer.allocate(256);
    final RequestCheck requestCheck;
    final BufferContext bufferContext;
    final RequestPipelineQueue requestPipelineQueue;
    IOHandler(RequestPipelineQueue requestPipelineQueue, Selector selector, SocketChannel socketChannel, RequestCheck requestCheck, BufferContext bufferContext) throws IOException {
        this.requestPipelineQueue = requestPipelineQueue;
        this.socketChannel = socketChannel;
        this.requestCheck = requestCheck;
        this.bufferContext = bufferContext;
        this.socketChannel.configureBlocking(false);
        selectionKey = this.socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selector.wakeup();
    }

    @Override
    public void handle() {
        try {
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isReadable()) {
                boolean alive = read();

                if (!alive) {
                    return;
                }
            }
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isWritable()) {
                send();
            }

        } catch (IOException | CancelledKeyException ex) {
            close();
        }
    }

    boolean read() throws IOException {
        int readCount = socketChannel.read(readBuffer);
        if (readCount == -1) {
            close();
            return false;
        }
        if (readCount == 0) {
            return true;
        }
        readBuffer.flip();
        ByteBuffer requestBuffer = bufferContext.getRequestBuffer();
        requestBuffer = BufferUtils.ensureCapacity(requestBuffer, readBuffer.remaining());
        bufferContext.setRequestBuffer(requestBuffer);
        RequestCheckResult result = requestCheck.check(readBuffer, requestBuffer);
        readBuffer.clear();
        if (!result.complete()) {
            return true;
        }
        requestBuffer.flip();
        requestBuffer.limit(result.requestLength());
        ByteBuffer requestSlice = requestBuffer.slice();
        requestPipelineQueue.put(new PipelineContext(socketChannel, "parse", requestSlice, result.headerLength(), bufferContext, selectionKey));
        return true;
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

    void send() throws IOException {
        ByteBuffer buffer = bufferContext.getResponseBuffer();
        socketChannel.write(buffer);
        if (buffer.hasRemaining()) {
            selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
            return;
        }
        bufferContext.setResponseBuffer(ByteBuffer.allocate(0));
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}