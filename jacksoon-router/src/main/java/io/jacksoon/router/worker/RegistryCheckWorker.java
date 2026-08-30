package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.router.connection.RegistryCheckManager;
import io.jacksoon.router.exception.RouterRegistryException;

public class RegistryCheckWorker implements Runnable {
    private final RegistryCheckManager registryCheckManager;
    private final long intervalMillis;
    private final ExceptionDispatcher exceptionDispatcher;

    public RegistryCheckWorker(RegistryCheckManager registryCheckManager, long intervalMillis, ExceptionDispatcher exceptionDispatcher) {
        this.registryCheckManager = registryCheckManager;
        this.intervalMillis = intervalMillis;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        refresh();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMillis);
                refresh();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    private void refresh() {
        try {
            registryCheckManager.refresh();
        } catch (Exception e) {
            RouterRegistryException failure = e instanceof RouterRegistryException registryException
                    ? registryException
                    : new RouterRegistryException("Registry refresh failed", e);
            exceptionDispatcher.dispatch(failure);
        }
    }
}