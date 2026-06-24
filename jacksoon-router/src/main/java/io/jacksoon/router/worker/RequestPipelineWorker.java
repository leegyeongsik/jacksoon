package io.jacksoon.router.worker;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.thread.Executor;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
public class RequestPipelineWorker implements Runnable {
    Executor executor;
    RequestPipelineQueue requestPipelineQueue;
    public RequestPipelineWorker(RequestPipelineQueue requestPipelineQueue, Executor executor) {
        this.requestPipelineQueue = requestPipelineQueue;
        this.executor = executor;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                PipelineContext pipelineContext = requestPipelineQueue.take();
                executor.executor(pipelineContext);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
