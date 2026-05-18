package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.context.PipelineContext;

public class RequestPipelineExecutor implements Executor{
    @Override
    public void executor(PipelineContext pipelineContext) {
        while (!pipelineContext.getEvent().isEmpty()){
            pipelineContext.setEvent(pipelineContext.getStep().next(pipelineContext));
        }
    }
}
