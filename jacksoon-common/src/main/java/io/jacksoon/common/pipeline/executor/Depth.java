package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

public interface Depth<T extends PipelineContext> extends PipelineExecutor<T> {
    void dodo(T context);
    @Override
    default void execute(T context) {
        dodo(context);
    }
}