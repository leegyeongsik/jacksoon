package io.jacksoon.registry.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineDispatcher;
import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
public class RegistryPipelineDispatcher implements PipelineDispatcher<RegistryPipelineContext> {
    private final RegistryPipelineExecutorRegistry registryPipelineExecutorRegistry;
    public RegistryPipelineDispatcher(RegistryPipelineExecutorRegistry registryPipelineExecutorRegistry) {
        this.registryPipelineExecutorRegistry = registryPipelineExecutorRegistry;
    }
    @Override
    public void dispatch(RegistryPipelineContext context) {
        String before = context.getEvent();
        if (before == null) {
            return;
        }
        PipelineExecutor<RegistryPipelineContext> executor = registryPipelineExecutorRegistry.get(before);
        executor.execute(context);
        if (before.equals(context.getEvent())) {
            context.setEvent(executor.nextEvent());
        }
    }
}