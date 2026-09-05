package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.router.connection.client.ClientConnectionManager;
import io.jacksoon.router.connection.client.ClientConnectionPolicy;
import io.jacksoon.router.connection.client.ClientConnectionTier;
import io.jacksoon.router.exception.RouterConfigurationException;

public class ClientConnectionMonitorWorker implements Runnable {
    private final ClientConnectionManager connectionManager;
    private final ClientConnectionTier tier;
    private final long checkIntervalMillis;
    private final ExceptionDispatcher exceptionDispatcher;

    public ClientConnectionMonitorWorker(ClientConnectionManager connectionManager, ClientConnectionPolicy connectionPolicy, ClientConnectionTier tier, ExceptionDispatcher exceptionDispatcher) {
        if (tier == ClientConnectionTier.CLOSE) {
            throw new RouterConfigurationException("CLOSE tier is handled by ClientConnectionCloseWorker");
        }
        this.connectionManager = connectionManager;
        this.tier = tier;
        this.checkIntervalMillis = connectionPolicy.checkIntervalMillis(tier);
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(checkIntervalMillis);
                connectionManager.inspect(tier, System.currentTimeMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }
}