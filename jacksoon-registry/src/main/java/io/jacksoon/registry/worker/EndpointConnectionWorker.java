package io.jacksoon.registry.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.EndPointConnectionManager;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.store.RegistryStore;

public class EndpointConnectionWorker implements Runnable {
    private final CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue;
    private final EndPointConnectionManager connectionManager;
    private final RegistryStore registryStore;

    public EndpointConnectionWorker(CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue, EndPointConnectionManager connectionManager, RegistryStore registryStore) {
        this.endpointConnectionQueue = endpointConnectionQueue;
        this.connectionManager = connectionManager;
        this.registryStore = registryStore;
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
                if (context != null) {
                    registryStore.removeEndpoint(context.getServiceName(), context.getInstanceId());
                }
                e.printStackTrace();
            }
        }
    }
}