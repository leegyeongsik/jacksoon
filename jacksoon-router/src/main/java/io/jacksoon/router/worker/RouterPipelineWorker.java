package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.exception.context.RouterExceptionContext;

public class RouterPipelineWorker implements Runnable {
    private final Executor<RouterPipelineContext> executor;
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;
    private final ExceptionDispatcher exceptionDispatcher;
    public RouterPipelineWorker(CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, Executor<RouterPipelineContext> executor, ExceptionDispatcher exceptionDispatcher) {
        this.routerPipelineQueue = routerPipelineQueue;
        this.executor = executor;
        this.exceptionDispatcher = exceptionDispatcher;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            RouterPipelineContext context = null;
            try {
                context = routerPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(RouterExceptionContext.of(context), e);
            }
        }
    }
}
