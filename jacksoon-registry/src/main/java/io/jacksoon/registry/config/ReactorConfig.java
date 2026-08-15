package io.jacksoon.registry.config;

import io.jacksoon.common.handler.AcceptHandler;
import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.selector.SelectorManager;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.RegistryRequestSubmitter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

@Init
public class ReactorConfig {
    @Init("registrySelector")
    public Selector registrySelector() throws IOException {
        return Selector.open();
    }

    @Init("registryServerSocket")
    public ServerSocketChannel registryServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Init
    public IOStore ioStore() {
        return new IOStore();
    }

    @Init("registryReactor")
    public Reactor registryReactor(@Init("registrySelector") Selector selector, @Init("registryServerSocket") ServerSocketChannel serverSocketChannel, @Init("registryAcceptHandler") Handler handler) throws Exception {
        Reactor reactor = new Reactor(selector);
        serverSocketChannel.socket().bind(new InetSocketAddress(1013));
        reactor.register(serverSocketChannel, handler, SelectionKey.OP_ACCEPT);
        return reactor;
    }
    @Init
    public SelectorManager selectorManager(){
        return new SelectorManager();
    }

    @Init("registryAcceptHandler")
    public Handler registryAcceptHandler(@Init("registryServerSocket") ServerSocketChannel serverSocketChannel, HttpRequestCheck check, RegistryRequestSubmitter submitter, IOStore ioStore,SelectorManager selectorManager) {
        return new AcceptHandler(serverSocketChannel, check, submitter, ioStore,selectorManager);
    }

    @Init("endpointSelector")
    public Selector endpointSelector() throws IOException {
        return Selector.open();
    }

    @Init("endpointReactor")
    public Reactor endpointReactor(@Init("endpointSelector") Selector selector) {
        return new Reactor(selector);
    }
}
