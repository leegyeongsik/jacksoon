package io.jacksoon.router.worker;

import io.jacksoon.router.connection.RegistryCheckManager;

public class RegistryCheckWorker implements Runnable {
    private final RegistryCheckManager registryCheckManager;
    private final long intervalMillis;

    public RegistryCheckWorker(RegistryCheckManager registryCheckManager, long intervalMillis) {
        this.registryCheckManager = registryCheckManager;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        registryCheckManager.refreshSafely();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMillis);
                registryCheckManager.refreshSafely();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}