package io.jacksoon.router.pipeline.step;


import io.jacksoon.router.pipeline.context.PipelineContext;

public class Step implements PipeLineStep {
    StepRegistry stepRegistry = new StepRegistry();
    @Override
    public String next(PipelineContext pipelineContext) {
        stepRegistry.getPipeLineExecutor(pipelineContext.getEvent()).executor(pipelineContext);
        return stepRegistry.getPipelineStep(pipelineContext.getEvent());
    }
}
