package io.jacksoon.registry.connection.event.util;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.registry.connection.event.EndPointEvent;

public record EndPointEventExecutorWarrap<T extends EndPointEvent>(String event, Executor<T> executor) {
}
