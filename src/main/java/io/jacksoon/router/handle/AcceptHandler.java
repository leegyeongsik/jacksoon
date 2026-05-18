package io.jacksoon.router.handle;

import io.jacksoon.router.handle.worker.CheckQueue;
import io.jacksoon.router.help.ConnectionContext;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler {
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;
    final HttpRequestCheck httpRequestCheck;
    final CheckQueue checkQueue;
    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel , HttpRequestCheck httpRequestCheck) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.httpRequestCheck = httpRequestCheck;
        this.checkQueue = new CheckQueue();

    }

    @Override
    public void handle() {
        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                new IOHandler(selector, socketChannel,httpRequestCheck,new ConnectionContext(),checkQueue);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}