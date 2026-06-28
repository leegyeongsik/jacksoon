package io.jacksoon.router.config;

import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.util.List;

@Init
public class PipelineConfig {

    @Init("routerPipeline")
    public Pipeline<RouterPipelineContext> routerPipeline(List<RouterDepth> depths) {
        return new Pipeline<>(depths);
    }
}

