package io.jacksoon.router.pipeline.executor;

import io.jacksoon.router.pipeline.context.PipelineContext;

public interface Depth extends PipeLineExecutor {
    void dodo(PipelineContext context);
    @Override
    default void executor(PipelineContext context) {
        dodo(context);
    }
}
