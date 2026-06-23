package io.jacksoon.router.seletor;

import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.init.annotation.Init;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
@Init
public class ReactorQueue {
    BlockingQueue<Handler> handlerQueue = new LinkedBlockingQueue<>();

    public Handler take() throws InterruptedException {
        return handlerQueue.take();
    }
    public void put(Handler handler ){
        handlerQueue.add(handler);
    }

}
