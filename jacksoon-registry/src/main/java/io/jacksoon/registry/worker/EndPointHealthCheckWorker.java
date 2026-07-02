package io.jacksoon.registry.worker;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

@Init
public class EndPointHealthCheckWorker implements Runnable {
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry;
    private final long intervalMillis = 5000L;

    public EndPointHealthCheckWorker(ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry) {
        this.endpointConnectionRegistry = endpointConnectionRegistry;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                fireHealthCheckEvents();
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void fireHealthCheckEvents() {
        for (EndPointConnectionHandler handler : endpointConnectionRegistry.handlers()) {
            handler.fireHealthCheckEvent();
        }
    }
}