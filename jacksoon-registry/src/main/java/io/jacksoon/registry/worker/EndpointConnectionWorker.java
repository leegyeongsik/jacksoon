package io.jacksoon.registry.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.EndPointConnectionManager;
import io.jacksoon.registry.connection.EndpointConnectionContext;

public class EndpointConnectionWorker implements Runnable {
    private final CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue;
    private final EndPointConnectionManager connectionManager;

    public EndpointConnectionWorker(
            CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue,
            EndPointConnectionManager connectionManager
    ) {
        this.endpointConnectionQueue = endpointConnectionQueue;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EndpointConnectionContext context = endpointConnectionQueue.take();
                connectionManager.connectAndCreate(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}