package io.jacksoon.common.pipeline.executor;

import io.jacksoon.common.pipeline.context.PipelineContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineExecutorRegistry<T extends PipelineContext> {
    private final Map<String, PipelineExecutor<T>> executorMap = new HashMap<>();

    public PipelineExecutorRegistry(List<? extends PipelineExecutor<T>> executors) {
        for (PipelineExecutor<T> executor : executors) {
            String event = executor.currentEvent();

            if (event == null || event.isBlank()) {
                throw new IllegalArgumentException(
                        "currentEvent is null or blank: " + executor.getClass().getName()
                );
            }

            if (executorMap.containsKey(event)) {
                throw new IllegalArgumentException("Duplicate event: " + event);
            }

            executorMap.put(event, executor);
        }
    }

    public PipelineExecutor<T> get(String event) {
        PipelineExecutor<T> executor = executorMap.get(event);

        if (executor == null) {
            throw new IllegalStateException("No executor for event: " + event);
        }

        return executor;
    }
}