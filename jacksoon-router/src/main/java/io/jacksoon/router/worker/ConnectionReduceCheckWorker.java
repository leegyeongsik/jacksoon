package io.jacksoon.router.worker;

import io.jacksoon.router.connection.BackendConnectionPoolManager;

public class ConnectionReduceCheckWorker implements Runnable {
    private final BackendConnectionPoolManager backendConnectionPoolManager;
    private final long intervalMillis;

    public ConnectionReduceCheckWorker(BackendConnectionPoolManager backendConnectionPoolManager, long intervalMillis) {
        this.backendConnectionPoolManager = backendConnectionPoolManager;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMillis);

                backendConnectionPoolManager.maintain();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}