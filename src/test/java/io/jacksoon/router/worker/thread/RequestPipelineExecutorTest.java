//package io.jacksoon.router.worker.thread;
//
//import io.jacksoon.router.pipeline.context.PipelineContext;
//import io.jacksoon.router.pipeline.step.Step;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class RequestPipelineExecutorTest {
//
//    @Test
//    void shouldRepeatedlyCallNextWhileEventIsNotEmpty() {
//        PipelineContext context = mock(PipelineContext.class);
//        Step step = mock(Step.class);
//
//        when(context.getEvent())
//                .thenReturn("READ")
//                .thenReturn("PARSE")
//                .thenReturn("");
//
//        when(context.getStep()).thenReturn(step);
//
//        when(step.next(context))
//                .thenReturn("PARSE")
//                .thenReturn("");
//
//        RequestPipelineExecutor executor = new RequestPipelineExecutor();
//
//        executor.executor(context);
//
//        verify(step, times(2)).next(context);
//        verify(context, times(2)).setEvent(anyString());
//
//        verify(context).setEvent("PARSE");
//        verify(context).setEvent("");
//    }
//
//    @Test
//    void shouldNotCallNextWhenInitialEventIsEmpty() {
//        PipelineContext context = mock(PipelineContext.class);
//
//        when(context.getEvent()).thenReturn("");
//
//        RequestPipelineExecutor executor = new RequestPipelineExecutor();
//
//        executor.executor(context);
//
//        verify(context, never()).getStep();
//        verify(context, never()).setEvent(any());
//    }
//}