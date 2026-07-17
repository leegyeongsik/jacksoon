package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.ProduceHint;
import io.jacksoon.console.dto.request.ServiceMetricProduceDto;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.stereotype.Component;

@Component
public class ServiceMetricEvent implements Executor<ServiceMetricProduceDto> {
    private final ConsoleService consoleService;
    public ServiceMetricEvent(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }
    @Override
    public void handle(ServiceMetricProduceDto requestDto) {
        consoleService.saveServiceMetric(requestDto);
    }
    @Override
    public ProduceHint event() {
        return ProduceHint.ROUTER_METRIC;
    }

    @Override
    public Class<ServiceMetricProduceDto> requestType() {
        return ServiceMetricProduceDto.class;
    }


}
