package io.jacksoon.router.worker.thread;

import io.jacksoon.router.pipeline.PipelineContext;

public class RequestPipelineExecutor implements Executor{
    @Override
    public void executor(PipelineContext pipelineContext) {
        while (!pipelineContext.event.isEmpty()){
            pipelineContext.event =  pipelineContext.step.next(pipelineContext.event);
        }
    }
}
