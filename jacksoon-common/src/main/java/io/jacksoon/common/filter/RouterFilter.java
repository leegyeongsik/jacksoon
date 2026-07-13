package io.jacksoon.common.filter;

import io.jacksoon.common.pipeline.context.PipelineContext;

public interface RouterFilter {
    void doFilter(PipelineContext context);
}
