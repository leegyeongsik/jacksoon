package io.jacksoon.console.event;

import io.jacksoon.console.dto.request.ProduceHint;
import io.jacksoon.console.dto.request.RegistryAction;
import io.jacksoon.console.dto.request.ServiceRuleProduceRequestDto;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.stereotype.Component;

@Component
public class ServiceRuleEvent implements Executor<ServiceRuleProduceRequestDto> {
    private final ConsoleService consoleService;

    public ServiceRuleEvent(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @Override
    public void handle(ServiceRuleProduceRequestDto requestDto) {
        if (requestDto.getAction() != RegistryAction.REGISTER_RULE) {
            throw new IllegalArgumentException("SERVICE_RULE only supports REGISTER_RULE action");
        }
        consoleService.replaceServiceRules(requestDto);
    }

    @Override
    public ProduceHint event() {
        return ProduceHint.SERVICE_RULE;
    }

    @Override
    public Class<ServiceRuleProduceRequestDto> requestType() {
        return ServiceRuleProduceRequestDto.class;
    }
}