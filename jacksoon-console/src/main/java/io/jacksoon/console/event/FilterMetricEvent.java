package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.FilterMetricProduceDto;
import io.jacksoon.console.dto.request.ProduceHint;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.stereotype.Component;
@Component
public class FilterMetricEvent implements Executor<FilterMetricProduceDto> {
    private final ConsoleService consoleService;

    public FilterMetricEvent(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @Override
    public void handle(FilterMetricProduceDto requestDto) {
        consoleService.saveFilterMetric(requestDto);
    }
    @Override
    public ProduceHint event() {
        return ProduceHint.FILTER_METRIC;
    }

    @Override
    public Class<FilterMetricProduceDto> requestType() {
        return FilterMetricProduceDto.class;
    }



}
