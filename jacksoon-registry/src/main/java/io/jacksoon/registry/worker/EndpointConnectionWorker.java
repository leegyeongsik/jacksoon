package io.jacksoon.registry.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.EndPointConnectionManager;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;
import io.jacksoon.registry.exception.RegistryConnectionException;

public class EndpointConnectionWorker implements Runnable {
    private final CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue;
    private final EndPointConnectionManager connectionManager;
    private final ExceptionDispatcher exceptionDispatcher;

    public EndpointConnectionWorker(CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue, EndPointConnectionManager connectionManager, ExceptionDispatcher exceptionDispatcher) {
        this.endpointConnectionQueue = endpointConnectionQueue;
        this.connectionManager = connectionManager;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            EndpointConnectionContext context = null;
            try {
                context = endpointConnectionQueue.take();
                connectionManager.connectAndCreate(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                RegistryConnectionException exception = e instanceof RegistryConnectionException registryConnectionException
                        ? registryConnectionException
                        : new RegistryConnectionException(
                        "Failed to connect endpoint" + describe(context),
                        e
                );
                exceptionDispatcher.dispatch(RegistryExceptionContext.of(context), exception);
            }
        }
    }
    private String describe(EndpointConnectionContext context) {
        if (context == null) {
            return "";
        }
        return ". serviceName=" + context.getServiceName() + ", instanceId=" + context.getInstanceId();
    }
}