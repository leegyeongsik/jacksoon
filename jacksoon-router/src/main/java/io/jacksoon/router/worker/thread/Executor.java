package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.context.PipelineContext;

public interface Executor {
    void executor(PipelineContext pipelineContext);
}
