package io.jacksoon.router.pipeline.step;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.executor.PipeLineExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class StepTest {

    @Test
    void executesCurrentEventExecutorAndReturnsNextEvent() {
        PipelineContext context = mock(PipelineContext.class);
        PipeLineExecutor executor = mock(PipeLineExecutor.class);
        StepRegistry stepRegistry = mock(StepRegistry.class);
        Step step = new Step(stepRegistry);

        when(context.getEvent()).thenReturn("parse");
        when(stepRegistry.getPipeLineExecutor("parse")).thenReturn(executor);
        when(stepRegistry.getPipelineStep("parse")).thenReturn("router");

        String result = step.next(context);

        verify(stepRegistry).getPipeLineExecutor("parse");
        verify(executor).executor(context);
        verify(stepRegistry).getPipelineStep("parse");
        Assertions.assertEquals("router", result);
    }
}
