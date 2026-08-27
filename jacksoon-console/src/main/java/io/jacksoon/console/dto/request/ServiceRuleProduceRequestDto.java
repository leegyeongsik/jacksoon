package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ServiceRuleProduceRequestDto extends BaseProduceDto {
    private String serviceName;
    private RegistryAction action;
    private List<ServiceRuleProduceDto> registryRuleDtoList;
    private long registryVersion;
}