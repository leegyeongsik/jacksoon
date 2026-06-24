package io.jacksoon.router.worker.connection;

import io.jacksoon.router.handle.ProxyContext;
import io.jacksoon.router.help.BufferContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class RequestBackendQueueTest {

    @Test
    void pollReturnsNullWhenQueueIsEmpty() throws Exception {
        RequestBackendQueue queue = new RequestBackendQueue();

        Assertions.assertNull(queue.poll());
        Assertions.assertTrue(queue.isEmpty());
    }

    @Test
    void putAndPollReturnSameObjectInFifoOrder() throws Exception {
        RequestBackendQueue queue = new RequestBackendQueue();
        ProxyContext first = proxyContext("first");
        ProxyContext second = proxyContext("second");

        queue.put(first);
        queue.put(second);

        Assertions.assertFalse(queue.isEmpty());
        Assertions.assertSame(first, queue.poll());
        Assertions.assertSame(second, queue.poll());
        Assertions.assertTrue(queue.isEmpty());
    }

    private ProxyContext proxyContext(String text) {
        return new ProxyContext(
                ByteBuffer.wrap(text.getBytes()),
                new BufferContext(),
                null
        );
    }
}
