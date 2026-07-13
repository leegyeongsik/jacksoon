package io.jacksoon.filterManagement.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.store.FilterStore;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FilterPipelineWorker implements Runnable {
    private final Executor<FilterPipelineContext> executor;
    private final CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue;
    private final FilterStore filterStore;

    public FilterPipelineWorker(CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue, Executor<FilterPipelineContext> executor, FilterStore filterStore) {
        this.filterPipelineQueue = filterPipelineQueue;
        this.executor = executor;
        this.filterStore = filterStore;
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
                e.printStackTrace();
                releaseLockQuietly(context);
                writeErrorQuietly(context, e);
            }
        }
    }

    private void releaseLockQuietly(FilterPipelineContext context) {
        if (context == null || !context.isUpdateLockHeld()) {
            return;
        }
        try {
            if (filterStore.isUpdateLockedByCurrentThread()) {
                filterStore.completeUpdate();
            }
        } finally {
            context.setUpdateLockHeld(false);
        }
    }

    private void writeErrorQuietly(FilterPipelineContext context, Exception e) {
        if (context == null) {
            return;
        }
        try {
            String message = e.getMessage() == null ? "Internal Server Error" : e.getMessage();
            boolean clientError = e instanceof IllegalArgumentException;
            context.getResponse().setStatusCode(clientError ? 400 : 500);
            context.getResponse().setReasonPhrase(clientError ? "Bad Request" : "Internal Server Error");
            context.getResponse().addHeader("Content-Type", "text/plain; charset=UTF-8");
            context.getResponse().setWriteBuffer(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            context.setEvent("write");
            executor.execute(context);
        } catch (Exception writeError) {
            writeError.printStackTrace();
        }
    }
}
