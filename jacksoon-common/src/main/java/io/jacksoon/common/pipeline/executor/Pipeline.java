package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pipeline<T extends PipelineContext> {
    private final Map<String, PipelineExecutor<T>> executorMap = new HashMap<>();
    public Pipeline(List<? extends PipelineExecutor<T>> executors) {
        for (PipelineExecutor<T> executor : executors) {
            executorMap.put(executor.currentEvent(), executor);
        }
    }
    public void execute(T context) {
        while (context.getEvent() != null) {
            PipelineExecutor<T> executor = executorMap.get(context.getEvent());

            if (executor == null) {
                throw new RuntimeException("No executor for event: " + context.getEvent());
            }
            executor.execute(context);
            context.setEvent(executor.nextEvent());
        }
    }
}