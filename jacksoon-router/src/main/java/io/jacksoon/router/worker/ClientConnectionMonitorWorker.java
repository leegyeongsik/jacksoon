package io.jacksoon.router.worker;

import io.jacksoon.router.connection.client.ClientConnectionManager;
import io.jacksoon.router.connection.client.ClientConnectionPolicy;
import io.jacksoon.router.connection.client.ClientConnectionTier;

public class ClientConnectionMonitorWorker implements Runnable {
    private final ClientConnectionManager connectionManager;
    private final ClientConnectionTier tier;
    private final long checkIntervalMillis;

    public ClientConnectionMonitorWorker(ClientConnectionManager connectionManager, ClientConnectionPolicy connectionPolicy, ClientConnectionTier tier) {
        if (tier == ClientConnectionTier.CLOSE) {
            throw new IllegalArgumentException("CLOSE tier is handled by ClientConnectionCloseWorker");
        }
        this.connectionManager = connectionManager;
        this.tier = tier;
        this.checkIntervalMillis = connectionPolicy.checkIntervalMillis(tier);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(checkIntervalMillis);
                connectionManager.inspect(tier, System.currentTimeMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}