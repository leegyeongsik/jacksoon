package io.jacksoon.registry.pipeline.executor;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

@Init
public class RegistryPipelineTaskExecutor implements Executor<RegistryPipelineContext> {
    private final Pipeline<RegistryPipelineContext> pipeline;

    public RegistryPipelineTaskExecutor(Pipeline<RegistryPipelineContext> pipeline) {
        this.pipeline = pipeline;

    }

    @Override
    public void execute(RegistryPipelineContext pipelineContext) {
        pipeline.execute(pipelineContext);
    }
}
