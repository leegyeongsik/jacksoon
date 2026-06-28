package io.jacksoon.common.handler;


import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpRequestCheck;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler {
    final Selector selector;
    final ServerSocketChannel serverSocketChannel;
    final HttpRequestCheck httpRequestCheck;
    final RequestSubmitter submitter;


    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel, HttpRequestCheck httpRequestCheck, RequestSubmitter submitter) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.httpRequestCheck = httpRequestCheck;
        this.submitter = submitter;
    }

    @Override
    public void handle() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();

            if (socketChannel != null) {
                new IOHandler(selector, socketChannel, httpRequestCheck, new BufferContext(), submitter);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}