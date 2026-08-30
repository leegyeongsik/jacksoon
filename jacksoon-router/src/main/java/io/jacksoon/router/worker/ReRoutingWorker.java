package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.router.pipeline.executor.router.HttpReRouter;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.exception.context.RouterExceptionContext;

public class ReRoutingWorker implements Runnable {
    private final CommonBlockingQueue<ReRoutingContext> reRoutingQueue;
    private final HttpReRouter httpReRouter;
    private final ExceptionDispatcher exceptionDispatcher;

    public ReRoutingWorker(CommonBlockingQueue<ReRoutingContext> reRoutingQueue, HttpReRouter httpReRouter, ExceptionDispatcher exceptionDispatcher) {
        this.reRoutingQueue = reRoutingQueue;
        this.httpReRouter = httpReRouter;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ReRoutingContext context = null;
            try {
                context = reRoutingQueue.take();
                httpReRouter.dodo(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                exceptionDispatcher.dispatch(RouterExceptionContext.of(context), e);
            }
        }
    }
}
