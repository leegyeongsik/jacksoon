package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.ProduceHint;
import io.jacksoon.console.dto.request.ServiceProduceDto;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.stereotype.Component;

@Component
public class ServiceEvent implements Executor<ServiceProduceDto> {
    private final ConsoleService consoleService;

    public ServiceEvent(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @Override
    public void handle(ServiceProduceDto requestDto) {
        switch (requestDto.getAction()) {
            case REGISTER -> consoleService.registerService(requestDto);
            case REMOVE -> consoleService.removeService(requestDto);
        }
    }
    @Override
    public ProduceHint event() {
        return ProduceHint.SERVICE;
    }

    @Override
    public Class<ServiceProduceDto> requestType() {
        return ServiceProduceDto.class;
    }


}
