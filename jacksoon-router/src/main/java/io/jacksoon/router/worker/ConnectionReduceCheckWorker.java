package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.router.connection.BackendConnectionPoolManager;

public class ConnectionReduceCheckWorker implements Runnable {
    private final BackendConnectionPoolManager backendConnectionPoolManager;
    private final long intervalMillis;
    private final ExceptionDispatcher exceptionDispatcher;

    public ConnectionReduceCheckWorker(BackendConnectionPoolManager backendConnectionPoolManager, long intervalMillis, ExceptionDispatcher exceptionDispatcher) {
        this.backendConnectionPoolManager = backendConnectionPoolManager;
        this.intervalMillis = intervalMillis;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(intervalMillis);
                backendConnectionPoolManager.maintain();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }
}