package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.step.Step;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class RequestPipelineExecutorTest {

    @Test
    void repeatedlyExecutesPipelineUntilEventIsEmpty() {
        PipelineContext context = mock(PipelineContext.class);
        Step step = mock(Step.class);
        RequestPipelineExecutor executor = new RequestPipelineExecutor(step);

        when(context.getEvent())
                .thenReturn("parse")
                .thenReturn("router")
                .thenReturn("");
        when(step.next(context))
                .thenReturn("router")
                .thenReturn("");

        executor.executor(context);

        verify(step, times(2)).next(context);
        verify(context).setEvent("router");
        verify(context).setEvent("");
    }

    @Test
    void doesNothingWhenInitialEventIsEmpty() {
        PipelineContext context = mock(PipelineContext.class);
        Step step = mock(Step.class);
        RequestPipelineExecutor executor = new RequestPipelineExecutor(step);

        when(context.getEvent()).thenReturn("");

        executor.executor(context);

        verify(step, never()).next(context);
        verify(context, never()).setEvent(anyString());
    }
}
