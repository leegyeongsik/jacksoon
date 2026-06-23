package io.jacksoon.router.worker.connection;

import io.jacksoon.router.handle.ProxyContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseBackendQueue {
    BlockingQueue<ProxyContext> proxyContextQueue = new LinkedBlockingQueue<>();

    public ProxyContext poll() throws InterruptedException {
        return proxyContextQueue.poll();
    }
    public void put(ProxyContext proxyContext ){
        proxyContextQueue.add(proxyContext);
    }

    public ProxyContext peek() {
        return proxyContextQueue.peek();
    }
    public boolean isEmpty(){
        return proxyContextQueue.isEmpty();
    }
}
