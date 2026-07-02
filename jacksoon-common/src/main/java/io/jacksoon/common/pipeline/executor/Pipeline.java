package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

public class Pipeline<T extends PipelineContext> {

    private final PipelineDispatcher<T> dispatcher;

    public Pipeline(PipelineDispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void execute(T context) {
        while (context.getEvent() != null) {
            dispatcher.dispatch(context);
        }
    }
}