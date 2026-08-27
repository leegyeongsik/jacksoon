package io.jacksoon.common.handler;

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
    final IOStore ioStore;
    final ClientConnectionLifecycle connectionLifecycle;

    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel, HttpRequestCheck httpRequestCheck, RequestSubmitter submitter, IOStore ioStore) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.httpRequestCheck = httpRequestCheck;
        this.submitter = submitter;
        this.ioStore = ioStore;
        this.connectionLifecycle = ClientConnectionLifecycle.NO_OP;
    }
    public AcceptHandler(Selector selector, ServerSocketChannel serverSocketChannel, HttpRequestCheck httpRequestCheck, RequestSubmitter submitter, IOStore ioStore, ClientConnectionLifecycle connectionLifecycle) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
        this.httpRequestCheck = httpRequestCheck;
        this.submitter = submitter;
        this.ioStore = ioStore;
        this.connectionLifecycle = connectionLifecycle;
    }

    @Override
    public void handle() {
        try {
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    return;
                }
                new IOHandler(selector, socketChannel, httpRequestCheck, submitter, ioStore, connectionLifecycle);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}