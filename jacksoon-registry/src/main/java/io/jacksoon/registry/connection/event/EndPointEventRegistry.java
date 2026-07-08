package io.jacksoon.registry.connection.event;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.event.util.EndPointEventExecutorWarrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Init
public class EndPointEventRegistry {
    private final Map<String, Executor<EndPointEvent>> eventExecutorMap = new HashMap<>();

    public EndPointEventRegistry(List<EndPointEventExecutorWarrap<EndPointEvent>> executorWarraps) {
        for (EndPointEventExecutorWarrap<EndPointEvent> executorWarrap : executorWarraps) {
            eventExecutorMap.put(executorWarrap.getEvent(), executorWarrap.getExecutor());
        }
    }

    public void execute(EndPointEvent endPointEvent) {
        Executor<EndPointEvent> executor = eventExecutorMap.get(endPointEvent.getReason());

        if (executor == null) {
            throw new IllegalArgumentException("Unknown endpoint event: " + endPointEvent.getReason());
        }
        executor.execute(endPointEvent);
    }
}
