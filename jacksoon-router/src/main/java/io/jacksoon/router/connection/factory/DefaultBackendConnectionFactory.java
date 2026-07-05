package io.jacksoon.router.connection.factory;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.handler.BackendIOHandler;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

@Init
public class DefaultBackendConnectionFactory implements BackendConnectionFactory {
    private final AtomicLong idGenerator = new AtomicLong();

    private final Selector backendSelector;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final HttpResponseCheck responseCheck;

    public DefaultBackendConnectionFactory(@Init("backendSelector") Selector backendSelector, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, HttpResponseCheck responseCheck){        this.backendSelector = backendSelector;
        this.routerPipelineQueue = routerPipelineQueue;
        this.responseCheck = responseCheck;
    }

    @Override
    public BackendIOHandler create(BackendConnectionPool pool) {
        EndpointSnapshot endpoint = pool.endpoint();

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));

            CommonBlockingQueue<ProxyContext> requestQueue = new CommonBlockingQueue<>();
            CommonBlockingQueue<ProxyContext> responseQueue = new CommonBlockingQueue<>();

            BackendIOHandler handler = new BackendIOHandler(
                    idGenerator.incrementAndGet(),
                    backendSelector,
                    socketChannel,
                    requestQueue,
                    responseQueue,
                    routerPipelineQueue,
                    responseCheck
            );

            handler.setConnectionPool(pool);
            return handler;

        } catch (IOException e) {
            throw new RuntimeException("Failed to create backend connection", e);
        }
    }
}