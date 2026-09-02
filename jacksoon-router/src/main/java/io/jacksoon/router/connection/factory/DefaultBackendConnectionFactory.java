package io.jacksoon.router.connection.factory;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.common.selector.SelectorManager;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPool;
import io.jacksoon.router.exception.BackendConnectionException;
import io.jacksoon.router.handler.BackendIOHandler;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.metric.ServiceMetricStore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

@Init
public class DefaultBackendConnectionFactory implements BackendConnectionFactory {
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final CommonBlockingQueue<ReRoutingContext> reRoutingQueue;
    private final HttpResponseCheck responseCheck;
    private final ExceptionDispatcher exceptionDispatcher;
    private final SelectorManager selectorManager;
    private final ServiceMetricStore serviceMetricStore;

    public DefaultBackendConnectionFactory(CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, CommonBlockingQueue<ReRoutingContext> reRoutingQueue, HttpResponseCheck responseCheck, ExceptionDispatcher exceptionDispatcher, SelectorManager selectorManager,@Init("serviceMetricStore") ServiceMetricStore serviceMetricStore) {
        this.routerPipelineQueue = routerPipelineQueue;
        this.reRoutingQueue = reRoutingQueue;
        this.responseCheck = responseCheck;
        this.exceptionDispatcher = exceptionDispatcher;
        this.selectorManager = selectorManager;
        this.serviceMetricStore = serviceMetricStore;
    }

    @Override
    public BackendIOHandler create(BackendConnectionPool pool) {
        EndpointSnapshot endpoint = pool.getEndpoint();
        String serviceName = pool.getServiceName();
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(endpoint.getHost(), endpoint.getPort()));
            CommonBlockingQueue<ProxyContext> requestQueue = new CommonBlockingQueue<>();
            BackendIOHandler handler = new BackendIOHandler(
                    serviceName,
                    selectorManager.nextSelector(),
                    socketChannel,
                    requestQueue,
                    routerPipelineQueue,
                    responseCheck,
                    reRoutingQueue,
                    exceptionDispatcher,
                    serviceMetricStore
            );
            handler.setConnectionPool(pool);
            return handler;
        } catch (IOException e) {
            throw new BackendConnectionException("Failed to create backend connection. serviceName=" + serviceName, e);
        }
    }
}