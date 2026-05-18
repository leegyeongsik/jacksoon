package io.jacksoon.router.pipeline.executor;

import io.jacksoon.router.pipeline.context.PipelineContext;

public interface PipeLineExecutor {
    void executor(PipelineContext context);
}
