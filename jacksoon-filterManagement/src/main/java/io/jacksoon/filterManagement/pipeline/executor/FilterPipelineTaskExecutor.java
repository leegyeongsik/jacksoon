package io.jacksoon.filterManagement.pipeline.executor;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterPipelineTaskExecutor implements Executor<FilterPipelineContext> {
    private final Pipeline<FilterPipelineContext> pipeline;

    public FilterPipelineTaskExecutor(Pipeline<FilterPipelineContext> pipeline) {
        this.pipeline = pipeline;

    }

    @Override
    public void execute(FilterPipelineContext pipelineContext) {
        pipeline.execute(pipelineContext);
    }
}
