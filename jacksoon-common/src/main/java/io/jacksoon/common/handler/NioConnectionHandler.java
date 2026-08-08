package io.jacksoon.common.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public abstract class NioConnectionHandler implements Handler, AutoCloseable {
    protected final Selector selector;
    protected volatile SocketChannel socketChannel;
    protected volatile SelectionKey selectionKey;

    protected NioConnectionHandler(Selector selector, SocketChannel socketChannel, int interestOps) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        try {
            this.selectionKey = socketChannel.register(selector, interestOps);
            this.selectionKey.attach(this);
            selector.wakeup();
        } catch (IOException e) {
            closeSocketOnly();
            throw new RuntimeException(e);
        }
    }
    protected void setInterestOps(int ops) {
        if (!selectionKey.isValid()) {
            return;
        }
        selectionKey.interestOps(ops);
        selector.wakeup();
    }
    @Override
    public void close() {
        try {
            selectionKey.cancel();
        } catch (Exception ignored) {
        }

        closeSocketOnly();
    }
    private void closeSocketOnly() {
        try {
            socketChannel.close();
        } catch (IOException ignored) {
        }
    }
}