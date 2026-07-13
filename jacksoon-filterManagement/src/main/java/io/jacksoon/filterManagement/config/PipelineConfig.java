package io.jacksoon.filterManagement.config;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.executor.FilterPipelineDispatcher;
import io.jacksoon.filterManagement.pipeline.executor.FilterPipelineExecutorRegistry;
import io.jacksoon.init.annotation.Init;

@Init
public class PipelineConfig {
    @Init
    public FilterPipelineDispatcher pipelineDispatcher(FilterPipelineExecutorRegistry registry) {
        return new FilterPipelineDispatcher(registry);
    }

    @Init("filterPipeline")
    public Pipeline<FilterPipelineContext> filterPipeline(FilterPipelineDispatcher dispatcher) {
        return new Pipeline<FilterPipelineContext>(dispatcher);
    }

}
