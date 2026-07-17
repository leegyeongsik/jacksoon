package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ServiceProduceDto extends BaseProduceDto {
    private String serviceName;
    RegistryAction action;
    List<InstanceProduceDto> registryInstanceDtoList;
    List<ServiceRuleProduceDto> registryRuleDtoList;

}