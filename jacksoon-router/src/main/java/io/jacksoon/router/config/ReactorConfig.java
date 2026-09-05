package io.jacksoon.router.config;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.handler.AcceptHandler;
import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.selector.SelectorManager;
import io.jacksoon.router.connection.client.ClientConnectionManager;
import io.jacksoon.router.connection.client.ClientConnectionPolicy;
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

    @Init("clientServerSocket")
    public ServerSocketChannel clientServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Init
    public ClientConnectionPolicy clientConnectionPolicy() {
        return new ClientConnectionPolicy(
                1_000L,
                5_000L,
                20_000L,
                60_000L,
                1.0,
                5.0,
                0.5,
                2.0
        );
    }

    @Init
    public IOStore ioStore() {
        return new IOStore();
    }

    @Init
    public ClientConnectionManager clientConnectionManager(ClientConnectionPolicy connectionPolicy) {
        return new ClientConnectionManager(connectionPolicy);
    }
    @Init
    public SelectorManager selectorManager(ExceptionDispatcher exceptionDispatcher){
        return new SelectorManager(exceptionDispatcher);
    }
    @Init("clientReactor")
    public Reactor clientReactor(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, @Init("acceptHandler") Handler handler, ExceptionDispatcher exceptionDispatcher, RouterProperties properties) throws Exception {
        Reactor reactor = new Reactor(selector, exceptionDispatcher);
        serverSocketChannel.socket().bind(new InetSocketAddress(properties.server().port()), properties.server().backlog());
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }

    @Init("acceptHandler")
    public Handler acceptHandler(@Init("clientSelector") Selector selector, @Init("clientServerSocket") ServerSocketChannel serverSocketChannel, HttpRequestCheck check, RequestSubmitter submitter, IOStore ioStore, ClientConnectionManager connectionManager) {
        return new AcceptHandler(selector, serverSocketChannel, check, submitter, ioStore, connectionManager);
    }
}
