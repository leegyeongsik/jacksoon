package io.jacksoon.router.handle;

import io.jacksoon.router.handle.worker.CheckContext;
import io.jacksoon.router.handle.worker.CheckQueue;
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
    int state = READING;
    final CheckQueue checkQueue;
    IOHandler( Selector selector, SocketChannel socketChannel, RequestCheck requestCheck, ConnectionContext connectionContext, CheckQueue checkQueue) throws IOException {
        this.socketChannel = socketChannel;
        this.requestCheck = requestCheck;
        this.connectionContext = connectionContext;
        this.checkQueue = checkQueue;
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
        checkQueue.put(new CheckContext(connectionContext.getRequestBuffer(),readBuffer,socketChannel,selectionKey));
    }

    void send() throws IOException {
        socketChannel.write(readBuffer);
        readBuffer.clear();
        selectionKey.interestOps(SelectionKey.OP_READ);
        state = READING;
    }
}