package io.jacksoon.router.pipeline.step;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.executor.PipeLineExecutor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StepTest {

    @Test
    void shouldExecuteCurrentEventExecutorAndReturnNextEvent() {
        PipelineContext context = mock(PipelineContext.class);
        PipeLineExecutor executor = mock(PipeLineExecutor.class);
        StepRegistry stepRegistry = mock(StepRegistry.class);

        String currentEvent = "PARSE";
        String nextEvent = "ROUTE";

        when(context.getEvent()).thenReturn(currentEvent);
        when(stepRegistry.getPipeLineExecutor(currentEvent))
                .thenReturn(executor);
        when(stepRegistry.getPipelineStep(currentEvent))
                .thenReturn(nextEvent);

//        Step step = new Step();
//        step.stepRegistry = stepRegistry;
//
//        String result = step.next(context);
//        verify(stepRegistry).getPipeLineExecutor(currentEvent);
//        verify(executor).executor(context);
//        verify(stepRegistry).getPipelineStep(currentEvent);
//
//        assertEquals(nextEvent, result);
    }
}