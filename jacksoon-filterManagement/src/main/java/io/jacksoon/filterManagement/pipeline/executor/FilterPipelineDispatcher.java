package io.jacksoon.filterManagement.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineDispatcher;
import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;

public class FilterPipelineDispatcher implements PipelineDispatcher<FilterPipelineContext> {
    private final FilterPipelineExecutorRegistry filterPipelineExecutorRegistry;
    public FilterPipelineDispatcher(FilterPipelineExecutorRegistry filterPipelineExecutorRegistry) {
        this.filterPipelineExecutorRegistry = filterPipelineExecutorRegistry;
    }
    @Override
    public void dispatch(FilterPipelineContext context) {
        String before = context.getEvent();
        if (before == null) {
            return;
        }
        PipelineExecutor<FilterPipelineContext> executor = filterPipelineExecutorRegistry.get(before);
        executor.execute(context);
        if (before.equals(context.getEvent())) {
            context.setEvent(executor.nextEvent());
        }
    }
}