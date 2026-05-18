package io.jacksoon.router.pipeline.step;


import io.jacksoon.router.pipeline.context.PipelineContext;

public interface PipeLineStep {
    String next(PipelineContext pipelineContext);
}