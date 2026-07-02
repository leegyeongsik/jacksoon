package io.jacksoon.router.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.util.List;
@Init
public class RouterPipelineExecutorRegistry extends PipelineExecutorRegistry<RouterPipelineContext> {
    public RouterPipelineExecutorRegistry(List<? extends PipelineExecutor<RouterPipelineContext>> pipelineExecutors) {
        super(pipelineExecutors);
    }
}
