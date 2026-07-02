package io.jacksoon.registry.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

import java.util.List;

@Init
public class RegistryPipelineExecutorRegistry extends PipelineExecutorRegistry<RegistryPipelineContext> {
    public RegistryPipelineExecutorRegistry(List<? extends PipelineExecutor<RegistryPipelineContext>> pipelineExecutors) {
        super(pipelineExecutors);
    }
}
