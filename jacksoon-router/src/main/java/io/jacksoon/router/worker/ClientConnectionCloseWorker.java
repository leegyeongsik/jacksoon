package io.jacksoon.router.worker;

import io.jacksoon.router.connection.client.ClientConnectionManager;

public class ClientConnectionCloseWorker implements Runnable {
    private final ClientConnectionManager connectionManager;
    public ClientConnectionCloseWorker(ClientConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                connectionManager.closeNext();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}