package io.jacksoon.router.worker.connection;

import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.init.annotation.Init;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
@Init
public class ConnectionQueue {
    BlockingQueue<ConnectionContext> connectionContextQueue = new LinkedBlockingQueue<>();

    public ConnectionContext take() throws InterruptedException {
        return connectionContextQueue.take();
    }
    public void put(ConnectionContext handler ){
        connectionContextQueue.add(handler);
    }

}
