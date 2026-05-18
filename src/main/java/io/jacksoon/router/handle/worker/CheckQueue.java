package io.jacksoon.router.handle.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CheckQueue {
    BlockingQueue<CheckContext> requestPipelineQueue = new LinkedBlockingQueue();

    public CheckContext take() throws InterruptedException {
        return requestPipelineQueue.take();
    }
    public void put(CheckContext checkContext ){
        requestPipelineQueue.add(checkContext);
    }

}
