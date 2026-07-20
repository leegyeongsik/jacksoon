package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.FilterProduceDto;
import io.jacksoon.console.dto.request.ProduceHint;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.stereotype.Component;

@Component
public class FilterEvent implements Executor<FilterProduceDto> {
    private final ConsoleService consoleService;

    public FilterEvent(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }


    @Override
    public void handle(FilterProduceDto requestDto) {
        if (requestDto.getFilterAction() == null) {
            throw new IllegalArgumentException("FilterAction must not be null");
        }
        switch (requestDto.getFilterAction()) {
            case FILTER_ACTIVE -> consoleService.activateFilter(requestDto);
            case FILTER_DELETE -> consoleService.deleteFilter(requestDto);
        }
    }
    @Override
    public ProduceHint event() {
        return ProduceHint.FILTER;
    }
    @Override
    public Class<FilterProduceDto> requestType() {
        return FilterProduceDto.class;
    }
}
