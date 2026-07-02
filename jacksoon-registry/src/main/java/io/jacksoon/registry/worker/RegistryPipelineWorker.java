package io.jacksoon.registry.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

public class RegistryPipelineWorker implements Runnable {
    Executor<RegistryPipelineContext> executor;
    CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue;

    public RegistryPipelineWorker(CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue, Executor<RegistryPipelineContext> executor) {
        this.registryPipelineQueue = registryPipelineQueue;
        this.executor = executor;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                RegistryPipelineContext context = registryPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
