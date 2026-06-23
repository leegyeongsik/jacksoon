package io.jacksoon.router.worker.connection;

import io.jacksoon.router.handle.ProxyContext;
import io.jacksoon.router.init.annotation.Init;

import java.lang.reflect.Proxy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
@Init
public class RequestBackendQueue {
    BlockingQueue<ProxyContext> proxyContextQueue = new LinkedBlockingQueue<>();

    public ProxyContext poll() throws InterruptedException {
        return proxyContextQueue.poll();
    }
    public void put(ProxyContext proxyContext ){
        proxyContextQueue.add(proxyContext);
    }
    public boolean isEmpty(){
        return proxyContextQueue.isEmpty();
    }
}
