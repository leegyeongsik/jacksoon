package io.jacksoon.registry.connection;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.connection.ConnectionManager;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Init
public class EndPointConnectionManager implements ConnectionManager<EndpointConnectionContext> {
    private final Selector endpointSelector;
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry;
    private final CommonBlockingQueue<EndPointEvent> endpointEventQueue;

    public EndPointConnectionManager(@Init("endpointSelector") Selector endpointSelector, ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry, CommonBlockingQueue<EndPointEvent> endpointEventQueue) {
        this.endpointSelector = endpointSelector;
        this.endpointConnectionRegistry = endpointConnectionRegistry;
        this.endpointEventQueue = endpointEventQueue;
    }

    @Override
    public void create(EndpointConnectionContext context, SocketChannel socketChannel) {
        EndpointConnection connection = new EndpointConnection(
                context.key(),
                context.getServiceName(),
                context.getInstanceId(),
                context.getHost(),
                context.getPort(),
                context.getHealthPath()
        );

        EndPointConnectionHandler handler = new EndPointConnectionHandler(
                endpointSelector,
                socketChannel,
                connection,
                endpointConnectionRegistry,
                endpointEventQueue
        );

        endpointConnectionRegistry.put(context.key(), handler);

        endpointSelector.wakeup();
    }
}