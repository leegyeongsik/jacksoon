package io.jacksoon.router.pipeline.step;


import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;
@Init
public class Step implements PipeLineStep {
    private final StepRegistry stepRegistry;

    public Step(StepRegistry stepRegistry) {
        this.stepRegistry = stepRegistry;
    }

    @Override
    public String next(PipelineContext pipelineContext) {
        stepRegistry.getPipeLineExecutor(pipelineContext.getEvent()).executor(pipelineContext);
        return stepRegistry.getPipelineStep(pipelineContext.getEvent());
    }
}
