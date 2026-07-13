package io.jacksoon.router.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

public class RouterPipelineWorker implements Runnable {
    Executor<RouterPipelineContext> executor;
    CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    public RouterPipelineWorker(CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, Executor<RouterPipelineContext> executor) {
        this.routerPipelineQueue = routerPipelineQueue;
        this.executor = executor;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                RouterPipelineContext context = routerPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
