package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferUtils;
import io.jacksoon.router.help.ConnectionContext;
import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.help.RequestCheckResult;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.step.Step;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class IOHandler implements Handler {
    static final int READING = 0, SENDING = 1;
    final SocketChannel socketChannel;
    final SelectionKey selectionKey;
    final ByteBuffer readBuffer = ByteBuffer.allocate(256);
    final RequestCheck requestCheck;
    final ConnectionContext connectionContext;
    final Step step = new Step();
    int state = READING;
    final RequestPipelineQueue requestPipelineQueue;

    IOHandler(RequestPipelineQueue requestPipelineQueue, Selector selector, SocketChannel socketChannel, RequestCheck requestCheck, ConnectionContext connectionContext) throws IOException {
        this.requestPipelineQueue = requestPipelineQueue;
        this.socketChannel = socketChannel;
        this.requestCheck = requestCheck;
        this.connectionContext = connectionContext;
        this.socketChannel.configureBlocking(false);
        selectionKey = this.socketChannel.register(selector, SelectionKey.OP_READ);
        selectionKey.attach(this);
        selector.wakeup();
    }

    @Override
    public void handle() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void read() throws IOException {
        int readCount = socketChannel.read(readBuffer);
        if (readCount <= 0) {
            return;
        }
        readBuffer.flip();
        ByteBuffer requestBuffer = connectionContext.getRequestBuffer();
        requestBuffer = BufferUtils.ensureCapacity(requestBuffer, readBuffer.remaining());
        connectionContext.setRequestBuffer(requestBuffer);
        RequestCheckResult result = requestCheck.check(readBuffer, requestBuffer);
        readBuffer.clear();
        if (!result.complete()) {
            return;
        }
        requestBuffer.flip();
        requestBuffer.limit(result.requestLength());
        ByteBuffer requestSlice = requestBuffer.slice();
        requestPipelineQueue.put(new PipelineContext(socketChannel, step, "READ", requestSlice, result.headerLength()));
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        state = SENDING;
    }

    void send() throws IOException {
        socketChannel.write(readBuffer);
        readBuffer.clear();
        selectionKey.interestOps(SelectionKey.OP_READ);
        state = READING;
    }
}