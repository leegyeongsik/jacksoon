package io.jacksoon.router.config;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

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

    @Init("clientReactor")
    public Reactor clientReactor(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, @Init("acceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1012));
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("acceptHandler")
    public AcceptHandler acceptHandler(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, RequestPipelineQueue queue, HttpRequestCheck check) {
        return new AcceptHandler(selector, serverSocketChannel, queue, check);
    }

    @Init("backendReactor")
    public Reactor backendReactor(@Init("backendSelector") Selector selector) throws Exception {
        return new Reactor(selector);}

}
