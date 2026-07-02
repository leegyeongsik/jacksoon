package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

public interface PipelineDispatcher<T extends PipelineContext> {
    void dispatch(T context);
}