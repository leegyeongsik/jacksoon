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
    private String serviceName;
    private RegistryAction action;
    private List<RegistryRuleProduceDto> registryRuleDtoList;
    private long registryVersion;

    public RegistryProduceRule(String serviceName,
                               RegistryAction registryAction,
                               List<RegistryRuleProduceDto> registryRuleDtoList,
                               long registryVersion) {
        super(ProduceHint.SERVICE_RULE, ProducerType.REGISTRY, Instant.now());
        this.serviceName = serviceName;
        this.action = registryAction;
        this.registryRuleDtoList = registryRuleDtoList;
        this.registryVersion = registryVersion;
    }
}