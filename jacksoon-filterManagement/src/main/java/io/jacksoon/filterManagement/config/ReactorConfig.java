package io.jacksoon.filterManagement.config;


import io.jacksoon.common.handler.AcceptHandler;
import io.jacksoon.common.handler.Handler;
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

    @Init("filterServerSocket")
    public ServerSocketChannel filterServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Init("filterReactor")
    public Reactor filterReactor(@Init("filterSelector") Selector selector, @Init("filterServerSocket") ServerSocketChannel serverSocketChannel, @Init("registryAcceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1013));
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("filterAcceptHandler")
    public Handler filterAcceptHandler(@Init("FilterSelector") Selector selector, @Init("filterServerSocket") ServerSocketChannel serverSocketChannel, HttpRequestCheck check, FilterRequestSubmitter submitter) {
        return new AcceptHandler(selector, serverSocketChannel, check, submitter);
    }
}
