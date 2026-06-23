package io.jacksoon.router.worker.thread;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.pipeline.context.PipelineContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class RequestPipelineQueueTest {

    @Test
    void takeReturnsSameObjectThatWasPut() throws Exception {
        RequestPipelineQueue queue = new RequestPipelineQueue();
        PipelineContext context = context("parse");

        queue.put(context);

        Assertions.assertSame(context, queue.take());
    }

    @Test
    void takeReturnsObjectsInFifoOrder() throws Exception {
        RequestPipelineQueue queue = new RequestPipelineQueue();
        PipelineContext first = context("parse");
        PipelineContext second = context("backend-response");

        queue.put(first);
        queue.put(second);

        Assertions.assertSame(first, queue.take());
        Assertions.assertSame(second, queue.take());
    }

    private PipelineContext context(String event) {
        return new PipelineContext(
                null,
                event,
                ByteBuffer.allocate(0),
                0,
                new BufferContext(),
                null
        );
    }
}
