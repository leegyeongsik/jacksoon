package io.jacksoon.router.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.io.IOException;
import java.nio.channels.SelectionKey;

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
            RouterPipelineContext context = null;
            try {
                context = routerPipelineQueue.take();
                executor.execute(context);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (context != null) {
                    closeClient(context.getSelectionKey());
                }
            }
        }
    }
    private void closeClient(SelectionKey selectionKey) {
        if (selectionKey == null) {
            return;
        }
        try {
            selectionKey.cancel();
        } catch (RuntimeException ignored) {
        }
        try {
            selectionKey.channel().close();
        } catch (IOException ignored) {
        }
    }
}
