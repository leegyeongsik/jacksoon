package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceProduceDto extends BaseProduceDto {
    private String serviceName;
    private RegistryAction action;
    private String instanceId;
    private String host;
    private int port;
    private String protocol;
    private String healthPath;
    private String reason;
    private long registryVersion;
}
