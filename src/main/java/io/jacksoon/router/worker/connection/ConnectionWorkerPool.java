package io.jacksoon.router.worker.connection;

import io.jacksoon.router.init.annotation.Init;

import java.nio.channels.Selector;

@Init
public class ConnectionWorkerPool {
    private final ConnectionManager connectionManager;
    private final ConnectionQueue connectionQueue;
    public ConnectionWorkerPool(ConnectionManager connectionManager, ConnectionQueue connectionQueue) {
        this.connectionManager = connectionManager;
        this.connectionQueue = connectionQueue;
    }

    public void start(){
        for (int i = 0; i < 1; i++) {
            new Thread(new ConnectionWorker(connectionQueue,connectionManager)).start();
        }
    }

}
