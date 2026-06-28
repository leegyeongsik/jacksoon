package io.jacksoon.router.connection;

import io.jacksoon.common.connection.ConnectionContexts;
import io.jacksoon.common.connection.ConnectionManager;
import io.jacksoon.common.connection.ConnectionRegistry;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handler.BackendIOHandler;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
@Init
public class RouterConnectionManager implements ConnectionManager {
    private final Selector selector;
    private final ConnectionRegistry connectionRegistry;
    private final HttpResponseCheck responseCheck;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue; // check이 저깄을텐데
    public RouterConnectionManager(@Init("backendSelector")Selector selector, ConnectionRegistry connectionRegistry, HttpResponseCheck responseCheck, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue) {
        this.selector = selector;
        this.connectionRegistry = connectionRegistry;
        this.responseCheck = responseCheck;
        this.routerPipelineQueue = routerPipelineQueue;
    }

    @Override
    public void create(SocketChannel socketChannel) {
        CommonBlockingQueue<ProxyContext> requestQueue =  new CommonBlockingQueue<>();
        CommonBlockingQueue<ProxyContext> responseQueue =  new CommonBlockingQueue<>();
        BackendIOHandler backendIOHandler =  new BackendIOHandler(selector,socketChannel,requestQueue,responseQueue,routerPipelineQueue,responseCheck);
        connectionRegistry.put("a", new ConnectionContexts(backendIOHandler.getSelectionKey(),requestQueue));
    }
}
