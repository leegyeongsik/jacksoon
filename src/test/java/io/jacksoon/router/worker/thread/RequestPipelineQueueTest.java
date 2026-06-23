//package io.jacksoon.router.worker.thread;
//
//import io.jacksoon.router.pipeline.context.PipelineContext;
//import io.jacksoon.router.pipeline.step.Step;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.SocketChannel;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//
//class RequestPipelineQueueTest {
//
//    @Test
//    void shouldReturnSameObjectThatWasAddedWithPut() throws Exception {
//        SocketChannel socketChannel = mock(SocketChannel.class);
//        Step step = mock(Step.class);
//        RequestPipelineQueue queue = new RequestPipelineQueue();
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//
//        PipelineContext context =
//                new PipelineContext(socketChannel, step, "test", byteBuffer, 15);
//
//        queue.put(context);
//        PipelineContext result = queue.take();
//
//        assertSame(context, result);
//    }
//
//    @Test
//    void shouldReturnObjectsInFifoOrderWhenMultipleObjectsAreAdded() throws Exception {
//        RequestPipelineQueue queue = new RequestPipelineQueue();
//        SocketChannel socketChannel = mock(SocketChannel.class);
//        Step step = mock(Step.class);
//        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//
//        PipelineContext first =
//                new PipelineContext(socketChannel, step, "test", byteBuffer, 15);
//        PipelineContext second =
//                new PipelineContext(socketChannel, step, "test", byteBuffer, 15);
//
//        queue.put(first);
//        queue.put(second);
//
//        assertSame(first, queue.take());
//        assertSame(second, queue.take());
//    }
//}