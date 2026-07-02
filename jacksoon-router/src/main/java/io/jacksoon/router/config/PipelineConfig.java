package io.jacksoon.router.config;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.RouterPipelineDispatcher;

@Init
public class PipelineConfig {
    @Init
    public RouterPipelineDispatcher pipelineDispatcher(PipelineExecutorRegistry<RouterPipelineContext> registry) {
        return new RouterPipelineDispatcher(registry);
    }
    @Init("routerPipeline")
    public Pipeline<RouterPipelineContext> routerPipeline(RouterPipelineDispatcher dispatcher) {
        return new Pipeline<RouterPipelineContext>(dispatcher);
    }


}

