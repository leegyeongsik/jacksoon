package io.jacksoon.router.worker.executor;


import io.jacksoon.common.pipeline.executor.Pipeline;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
@Init
public class RouterPipelineTaskExecutor implements Executor<RouterPipelineContext> {
    private final Pipeline<RouterPipelineContext> pipeline;
    public RouterPipelineTaskExecutor(Pipeline<RouterPipelineContext> pipeline) {
        this.pipeline = pipeline;
    }
    @Override
    public void execute(RouterPipelineContext pipelineContext) {
        pipeline.execute(pipelineContext);
    }
}
