package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
public class AcceptHandler implements Handler {
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;
    final RequestPipelineQueue requestPipelineQueue;
    final HttpRequestCheck httpRequestCheck;
    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel, RequestPipelineQueue requestPipelineQueue , HttpRequestCheck httpRequestCheck) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.requestPipelineQueue = requestPipelineQueue;
        this.httpRequestCheck = httpRequestCheck;
    }
    @Override
    public void handle() {
        try {
            final SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                new IOHandler(requestPipelineQueue,selector, socketChannel,httpRequestCheck,new BufferContext());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}