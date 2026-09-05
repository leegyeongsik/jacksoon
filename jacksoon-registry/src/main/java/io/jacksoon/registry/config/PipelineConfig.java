package io.jacksoon.registry.config;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.executor.RegistryPipelineDispatcher;
import io.jacksoon.registry.pipeline.executor.RegistryPipelineExecutorRegistry;

@Init
public class PipelineConfig {
    @Init
    public RegistryPipelineDispatcher pipelineDispatcher(RegistryPipelineExecutorRegistry registry) {
        return new RegistryPipelineDispatcher(registry);
    }

    @Init("registryPipeline")
    public Pipeline<RegistryPipelineContext> routerPipeline(RegistryPipelineDispatcher dispatcher) {
        return new Pipeline<RegistryPipelineContext>(dispatcher);
    }

}
