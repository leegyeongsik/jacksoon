package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.router.connection.client.ClientConnectionManager;

public class ClientConnectionCloseWorker implements Runnable {
    private final ClientConnectionManager connectionManager;
    private final ExceptionDispatcher exceptionDispatcher;
    public ClientConnectionCloseWorker(ClientConnectionManager connectionManager, ExceptionDispatcher exceptionDispatcher) {
        this.connectionManager = connectionManager;
        this.exceptionDispatcher = exceptionDispatcher;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                connectionManager.closeNext();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }
}