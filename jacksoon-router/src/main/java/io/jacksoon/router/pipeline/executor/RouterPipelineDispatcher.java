package io.jacksoon.router.pipeline.executor;

import io.jacksoon.common.pipeline.executor.PipelineDispatcher;
import io.jacksoon.common.pipeline.executor.PipelineExecutor;
import io.jacksoon.common.pipeline.executor.PipelineExecutorRegistry;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

public class RouterPipelineDispatcher implements PipelineDispatcher<RouterPipelineContext> {

    private final PipelineExecutorRegistry<RouterPipelineContext> executorRegistry;

    public RouterPipelineDispatcher(PipelineExecutorRegistry<RouterPipelineContext> executorRegistry) {
        this.executorRegistry = executorRegistry;
    }

    @Override
    public void dispatch(RouterPipelineContext context) {
        PipelineExecutor<RouterPipelineContext> executor =
                executorRegistry.get(context.getEvent());
        executor.execute(context);
        context.setEvent(executor.nextEvent());
    }
}