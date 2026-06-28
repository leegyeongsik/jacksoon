package io.jacksoon.router.worker.worker;

import io.jacksoon.common.connection.ConnectionContext;
import io.jacksoon.common.connection.ConnectionManager;
import io.jacksoon.common.util.CommonBlockingQueue;

import java.nio.channels.SocketChannel;

public class ConnectionWorker implements Runnable{
    private final ConnectionManager connectionManager;
    private final CommonBlockingQueue<ConnectionContext> connectionContextQueue;

    public ConnectionWorker(ConnectionManager connectionManager, CommonBlockingQueue<ConnectionContext> connectionContextQueue) {
        this.connectionManager = connectionManager;
        this.connectionContextQueue = connectionContextQueue;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ConnectionContext connectionContext = connectionContextQueue.take();
                SocketChannel socketChannel = connectionManager.connect(connectionContext);
                connectionManager.create(socketChannel);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
