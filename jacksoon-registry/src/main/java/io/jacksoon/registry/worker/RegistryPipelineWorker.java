package io.jacksoon.registry.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;

public class RegistryPipelineWorker implements Runnable {
    private final Executor<RegistryPipelineContext> executor;
    private final CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue;
    private final ExceptionDispatcher exceptionDispatcher;

    public RegistryPipelineWorker(CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue, Executor<RegistryPipelineContext> executor, ExceptionDispatcher exceptionDispatcher) {
        this.registryPipelineQueue = registryPipelineQueue;
        this.executor = executor;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            RegistryPipelineContext context = null;
            try {
                context = registryPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(RegistryExceptionContext.of(context), e);
            }
        }
    }
}
