package io.jacksoon.filterManagement.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

import java.util.List;

@Init
public class FilterPipelineExecutorRegistry extends PipelineExecutorRegistry<FilterPipelineContext> {
    public FilterPipelineExecutorRegistry(List<? extends PipelineExecutor<FilterPipelineContext>> pipelineExecutors) {
        super(pipelineExecutors);
    }
}
