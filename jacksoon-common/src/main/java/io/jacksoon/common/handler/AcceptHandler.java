package io.jacksoon.common.handler;


import io.jacksoon.common.selector.SelectorManager;
import io.jacksoon.common.util.HttpRequestCheck;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class AcceptHandler implements Handler {
    final ServerSocketChannel serverSocketChannel;
    final HttpRequestCheck httpRequestCheck;
    final RequestSubmitter submitter;
    final IOStore ioStore;
    final SelectorManager selectorManager;
    public AcceptHandler(ServerSocketChannel serverSocketChannel, HttpRequestCheck httpRequestCheck, RequestSubmitter submitter, IOStore ioStore, SelectorManager selectorManager) {
        this.serverSocketChannel = serverSocketChannel;
        this.httpRequestCheck = httpRequestCheck;
        this.submitter = submitter;
        this.ioStore = ioStore;
        this.selectorManager = selectorManager;
    }

    @Override
    public void handle() {
        try {
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel == null) {
                    return;
                }
                new IOHandler(selectorManager.nextSelector(), socketChannel, httpRequestCheck, submitter,ioStore); ;

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}