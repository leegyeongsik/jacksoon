package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.BaseProduceDto;
import io.jacksoon.console.dto.request.ProduceHint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EventRegistry {

    private final Map<ProduceHint, Executor<? extends BaseProduceDto>> executorMap = new EnumMap<>(ProduceHint.class);

    public EventRegistry(List<Executor<? extends BaseProduceDto>> executors) {
        for (Executor<? extends BaseProduceDto> executor : executors) {
            ProduceHint event = executor.event();
            if (event == null) {
                throw new IllegalArgumentException();
            }
            Executor<? extends BaseProduceDto> previous = executorMap.putIfAbsent(event, executor);
            if (previous != null) {
                throw new IllegalArgumentException();
            }
        }
    }
    public void execute(BaseProduceDto requestDto) {
        Executor<? extends BaseProduceDto> executor = executorMap.get(requestDto.getHint());
        if (executor == null) {
            throw new IllegalStateException("No executor for event: " + requestDto.getHint());
        }
        executor.execute(requestDto);
    }
}