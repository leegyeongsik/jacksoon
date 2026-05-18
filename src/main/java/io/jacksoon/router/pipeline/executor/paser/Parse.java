package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.executor.PipeLineExecutor;

public interface Parse extends PipeLineExecutor {
    void parse(PipelineContext context);
    @Override
    default void executor(PipelineContext context) {
        parse(context);
    }
}
