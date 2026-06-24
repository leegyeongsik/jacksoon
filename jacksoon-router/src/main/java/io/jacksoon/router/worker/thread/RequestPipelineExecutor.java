package io.jacksoon.router.worker.thread;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.step.Step;
@Init
public class RequestPipelineExecutor implements Executor{
    private final Step step;
    public RequestPipelineExecutor(Step step) {
        this.step = step;
    }
    @Override
    public void executor(PipelineContext pipelineContext) {
        while (!pipelineContext.getEvent().isEmpty()){
            pipelineContext.setEvent(step.next(pipelineContext));
        }
    }
}
