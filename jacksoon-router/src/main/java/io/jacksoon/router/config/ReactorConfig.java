package io.jacksoon.router.config;

import io.jacksoon.common.handler.AcceptHandler;
import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.init.annotation.Init;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

@Init
public class ReactorConfig {
    @Init("clientSelector")
    public Selector clientSelector() throws IOException {
        return Selector.open();
    }

    @Init("backendSelector")
    public Selector backendSelector() throws IOException {
        return Selector.open();
    }

    @Init("clientServerSocket")
    public ServerSocketChannel clientServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }
    @Init()
    public IOStore ioStore(){
        return new IOStore();
    }
    @Init("clientReactor")
    public Reactor clientReactor(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, @Init("acceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1012), 512);
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("acceptHandler")
    public Handler acceptHandler(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, HttpRequestCheck check, RequestSubmitter submitter,IOStore ioStore) {
        return new AcceptHandler(selector, serverSocketChannel, check, submitter,ioStore);
    }

    @Init("backendReactor")
    public Reactor backendReactor(@Init("backendSelector") Selector selector) throws Exception {
        return new Reactor(selector);
    }
}
