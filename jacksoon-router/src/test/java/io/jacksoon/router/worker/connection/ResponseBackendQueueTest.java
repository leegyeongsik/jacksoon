//package io.jacksoon.router.worker.connection;
//
//import io.jacksoon.router.help.BufferContext;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//
//class ResponseBackendQueueTest {
//
//    @Test
//    void peekDoesNotRemoveObject() throws Exception {
//        ResponseBackendQueue queue = new ResponseBackendQueue();
//        ProxyContext proxyContext = proxyContext("request");
//
//        queue.put(proxyContext);
//
//        Assertions.assertSame(proxyContext, queue.peek());
//        Assertions.assertFalse(queue.isEmpty());
//        Assertions.assertSame(proxyContext, queue.poll());
//        Assertions.assertTrue(queue.isEmpty());
//    }
//
//    @Test
//    void pollReturnsObjectsInFifoOrder() throws Exception {
//        ResponseBackendQueue queue = new ResponseBackendQueue();
//        ProxyContext first = proxyContext("first");
//        ProxyContext second = proxyContext("second");
//
//        queue.put(first);
//        queue.put(second);
//
//        Assertions.assertSame(first, queue.poll());
//        Assertions.assertSame(second, queue.poll());
//        Assertions.assertNull(queue.poll());
//    }
//
//    private ProxyContext proxyContext(String text) {
//        return new ProxyContext(
//                ByteBuffer.wrap(text.getBytes()),
//                new BufferContext(),
//                null
//        );
//    }
//}
