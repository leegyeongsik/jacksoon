package io.jacksoon.router.seletor;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;
public class Reactor implements Runnable{
    final ServerSocketChannel serverSocketChannel;
    final Selector selector;
    final AcceptHandler acceptHandler;
    public Reactor(Selector selector, ServerSocketChannel serverSocketChannel, int port, AcceptHandler acceptHandler) throws IOException {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
        this.acceptHandler = acceptHandler;
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register( selector,SelectionKey.OP_ACCEPT);

        selectionKey.attach(acceptHandler);
    }
    @Override
    public void run() {
        try {
            while (true) {
                processOnce();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    public void processOnce() throws IOException {
        selector.select();
        Set<SelectionKey> selected = selector.selectedKeys();
        for (SelectionKey key : selected) {
            dispatch(key);
        }
        selected.clear();
    }
     void dispatch(SelectionKey selectionKey) {
        Handler handler = (Handler) selectionKey.attachment();
        handler.handle();
    }
}
