package io.jacksoon.router.worker.thread;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
@Init
public class RequestPipelineQueue {
    BlockingQueue<PipelineContext> requestPipelineQueue = new LinkedBlockingQueue();

    public PipelineContext take() throws InterruptedException {
        return requestPipelineQueue.take();
    }
    public void put(PipelineContext pipelineContext ){
        requestPipelineQueue.add(pipelineContext);
    }
}
