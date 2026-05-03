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
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;
    final RequestPipelineQueue requestPipelineQueue;
    public Reactor(int port, RequestPipelineQueue requestPipelineQueue) throws IOException {
        this.requestPipelineQueue = requestPipelineQueue;
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Attach a handler to handle when an event occurs in ServerSocketChannel.
        selectionKey.attach(new AcceptHandler(selector, serverSocketChannel,requestPipelineQueue));
    }
    @Override
    public void run() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey selectionKey : selected) {
                    dispatch(selectionKey);
                }
                selected.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void dispatch(SelectionKey selectionKey) {
        Handler handler = (Handler) selectionKey.attachment();
        handler.handle();
    }
}
