//package io.jacksoon.router.worker;
//
//import io.jacksoon.router.worker.thread.Executor;
//import io.jacksoon.router.worker.thread.RequestPipelineQueue;
//import org.junit.jupiter.api.Test;
//
//import static org.mockito.Mockito.*;
//
//class RequestPipelineWorkerTest {
//    @Test
//    void run__executor() throws Exception {
//        RequestPipelineQueue queue = mock(RequestPipelineQueue.class);
//        Executor executor = mock(Executor.class);
//        PipelineContext context = mock(PipelineContext.class);
//
//        when(queue.take()).thenReturn(context)
//                .thenThrow(new InterruptedException());
//
//        RequestPipelineWorker worker =
//                new RequestPipelineWorker(queue, executor);
//
//        worker.run();
//
//        verify(queue, times(2)).take();
//        verify(executor, times(1)).executor(context);
//        verifyNoMoreInteractions(executor);
//    }
//
//    @Test
//    void run_InterruptedException_() throws Exception {
//        RequestPipelineQueue queue = mock(RequestPipelineQueue.class);
//        Executor executor = mock(Executor.class);
//
//        when(queue.take()).thenThrow(new InterruptedException());
//
//        RequestPipelineWorker worker =
//                new RequestPipelineWorker(queue, executor);
//
//        worker.run();
//
//        verify(queue, times(1)).take();
//        verifyNoInteractions(executor);
//
//        assert Thread.currentThread().isInterrupted();
//
//        Thread.interrupted();
//    }
//}