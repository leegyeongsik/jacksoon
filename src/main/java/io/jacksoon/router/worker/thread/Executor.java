package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.PipelineContext;

public interface Executor {
    void executor(PipelineContext pipelineContext);
}
