package io.jacksoon.registry.connection.event.util;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.registry.connection.event.EndPointEvent;
import lombok.Getter;

@Getter
public class EndPointEventExecutorWarrap<T extends EndPointEvent> {
    private final String event;
    private final Executor<T> executor;

    public EndPointEventExecutorWarrap(String event, Executor<T> executor) {
        this.event = event;
        this.executor = executor;
    }

    public void execute(T endPointEvent) {
        executor.execute(endPointEvent);
    }
}
