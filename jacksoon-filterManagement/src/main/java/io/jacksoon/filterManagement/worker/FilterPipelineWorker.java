package io.jacksoon.filterManagement.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;

public class FilterPipelineWorker implements Runnable {
    private final Executor<FilterPipelineContext> executor;
    private final CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue;
    private final ExceptionDispatcher exceptionDispatcher;

    public FilterPipelineWorker(CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue, Executor<FilterPipelineContext> executor, ExceptionDispatcher exceptionDispatcher) {
        this.filterPipelineQueue = filterPipelineQueue;
        this.executor = executor;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            FilterPipelineContext context = null;
            try {
                context = filterPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(context, e);
            }
        }
    }
}
