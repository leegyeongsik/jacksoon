package io.jacksoon.router.pipeline.step;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.executor.PipeLineExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Init
public class StepRegistry {
    Map<String,PipeLineExecutor> pipeLineMap = new HashMap<>();
    public StepRegistry(List<PipeLineExecutor> pipeLineExecutors){
        for (PipeLineExecutor pipeLineExecutor : pipeLineExecutors) {
            pipeLineMap.put(pipeLineExecutor.currentEvent(),pipeLineExecutor);
        }
    }

    public PipeLineExecutor getPipeLineExecutor(String depth) {
        return pipeLineMap.get(depth);
    }
    public String getPipelineStep(String depth) {
        return pipeLineMap.get(depth).nextEvent();
    }
}
