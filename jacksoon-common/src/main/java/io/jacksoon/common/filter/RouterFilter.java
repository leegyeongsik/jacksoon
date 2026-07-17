package io.jacksoon.common.filter;

import io.jacksoon.common.pipeline.context.PipelineContext;

public interface RouterFilter {
    default boolean isSupport(PipelineContext context) {
        return true;
    }

    void doFilter(PipelineContext context);
}
