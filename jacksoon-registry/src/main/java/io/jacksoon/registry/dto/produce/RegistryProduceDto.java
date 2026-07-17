package io.jacksoon.registry.dto.produce;

import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class RegistryProduceDto extends ProduceDto {
    private RegistryAction action;
    private String serviceName;
    private String instanceId;
    private String host;
    private int port;
    private String protocol;
    private String healthPath;
    private String reason;
}
