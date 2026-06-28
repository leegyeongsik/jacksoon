package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

public interface PipelineExecutor<T extends PipelineContext> {
    void execute(T context);
    String currentEvent();
    String nextEvent();
}
