package io.jacksoon.router.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.router.pipeline.executor.router.HttpReRouter;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;

public class ReRoutingWorker implements Runnable{
    private final CommonBlockingQueue<ReRoutingContext> reRoutingQueue;
    private final HttpReRouter httpReRouter;
    public ReRoutingWorker(CommonBlockingQueue<ReRoutingContext> reRoutingQueue, HttpReRouter httpReRouter) {
        this.reRoutingQueue = reRoutingQueue;
        this.httpReRouter = httpReRouter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ReRoutingContext reRoutingContext = reRoutingQueue.take();
                httpReRouter.dodo(reRoutingContext);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
