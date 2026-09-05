package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.BaseProduceDto;
import io.jacksoon.console.dto.request.ProduceHint;

public interface Executor<T> {
    void handle(T requestDto);
    ProduceHint event();
    Class<T> requestType();
    default void execute(BaseProduceDto requestDto) {
        if (!requestType().isInstance(requestDto)) {
            throw new IllegalArgumentException();
        }
        handle(requestType().cast(requestDto));
    }
}
