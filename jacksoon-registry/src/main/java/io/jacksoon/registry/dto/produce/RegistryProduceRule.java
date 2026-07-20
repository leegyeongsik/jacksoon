package io.jacksoon.registry.dto.produce;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistryProduceRule extends ProduceDto {
    String serviceName;
    RegistryAction action;
    List<RegistryRuleProduceDto> registryRuleDtoList;

    public RegistryProduceRule(String serviceName, RegistryAction registryAction, List<RegistryRuleProduceDto> registryRuleDtoList) {
        super(ProduceHint.SERVICE_RULE,ProducerType.REGISTRY,Instant.now());
        this.serviceName = serviceName;
        this.action = registryAction;
        this.registryRuleDtoList = registryRuleDtoList;

    }
}