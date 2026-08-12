package io.jacksoon.filterManagement.config;


import io.jacksoon.common.handler.AcceptHandler;
import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.filterManagement.handle.FilterRequestSubmitter;
import io.jacksoon.init.annotation.Init;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

@Init
public class ReactorConfig {
    @Init("FilterSelector")
    public Selector FilterSelector() throws IOException {
        return Selector.open();
    }

    @Init("FilterServerSocket")
    public ServerSocketChannel filterServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Init
    public IOStore ioStore() {
        return new IOStore();
    }

    @Init("FilterReactor")
    public Reactor filterReactor(@Init("FilterSelector") Selector selector, @Init("FilterServerSocket") ServerSocketChannel serverSocketChannel, @Init("FilterAcceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1011));
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("FilterAcceptHandler")
    public Handler filterAcceptHandler(@Init("FilterSelector") Selector selector, @Init("FilterServerSocket") ServerSocketChannel serverSocketChannel, HttpRequestCheck check, FilterRequestSubmitter submitter, IOStore ioStore) {
        return new AcceptHandler(selector, serverSocketChannel, check, submitter, ioStore);
    }
}
