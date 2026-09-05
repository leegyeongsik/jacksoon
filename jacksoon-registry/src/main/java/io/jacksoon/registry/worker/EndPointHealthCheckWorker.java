package io.jacksoon.registry.worker;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

@Init
public class EndPointHealthCheckWorker implements Runnable {
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry;
    private final ExceptionDispatcher exceptionDispatcher;
    private final long intervalMillis = 5000L;

    public EndPointHealthCheckWorker(ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry, ExceptionDispatcher exceptionDispatcher) {
        this.endpointConnectionRegistry = endpointConnectionRegistry;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            fireHealthCheckEvents();
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void fireHealthCheckEvents() {
        for (EndPointConnectionHandler handler : endpointConnectionRegistry.handlers()) {
            try {
                handler.fireHealthCheckEvent();
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }
}