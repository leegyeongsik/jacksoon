package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.context.PipelineContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestPipelineQueue {
    BlockingQueue<PipelineContext> requestPipelineQueue = new LinkedBlockingQueue();

    public PipelineContext take() throws InterruptedException {
        return requestPipelineQueue.take();
    }
    public void put(PipelineContext pipelineContext ){
        requestPipelineQueue.add(pipelineContext);
    }
}
