
package io.jacksoon.common.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public abstract class NioConnectionHandler implements Handler, AutoCloseable {

    protected final Selector selector;
    protected volatile SocketChannel socketChannel;
    protected volatile SelectionKey selectionKey;

    protected NioConnectionHandler(Selector selector, SocketChannel socketChannel) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        try {
            socketChannel.configureBlocking(false);
            this.selectionKey = socketChannel.register(selector, 0, this);
        } catch (IOException e) {
            closeSocketOnly();
            throw new RuntimeException(e);
        }
    }

    protected void setInterestOps(int ops) {
        SelectionKey key = selectionKey;
        if (key == null || !key.isValid()) {
            return;
        }
        key.interestOps(ops);
        selector.wakeup();
    }

    @Override
    public void close() {
        SelectionKey key = selectionKey;

        if (key != null) {
            try {
                key.cancel();
            } catch (Exception ignored) {
            }
        }

        closeSocketOnly();
    }

    private void closeSocketOnly() {
        SocketChannel channel = socketChannel;

        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }
}
