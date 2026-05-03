package io.jacksoon.router.pipeline.step;


import io.jacksoon.router.pipeline.PipeLineStep;
import io.jacksoon.router.pipeline.StepRegistry;

public class Step implements PipeLineStep {
    StepRegistry stepRegistry;
    @Override
    public String next(String event) {
        stepRegistry.getPipeLineExecutor(event).executor();
        return stepRegistry.getPipelineStep(event);
    }
}
