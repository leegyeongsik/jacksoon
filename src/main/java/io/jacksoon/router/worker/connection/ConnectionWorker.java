package io.jacksoon.router.worker.connection;

public class ConnectionWorker implements Runnable {
    private final ConnectionQueue connectionQueue;
    private final ConnectionManager connectionManager;

    public ConnectionWorker(ConnectionQueue connectionQueue, ConnectionManager connectionManager) {
        this.connectionQueue = connectionQueue;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        while (true){
            try {
                ConnectionContext connectionContext =  connectionQueue.take();
                connectionManager.create(connectionContext);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
