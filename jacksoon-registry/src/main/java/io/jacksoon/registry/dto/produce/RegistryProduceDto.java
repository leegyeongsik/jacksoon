package io.jacksoon.registry.dto.produce;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import io.jacksoon.registry.store.entity.RegisteredEndpoint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
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
    private long registryVersion;

    public RegistryProduceDto(RegistryAction action,
                              RegisteredEndpoint endpoint,
                              String reason,
                              long registryVersion,
                              ProduceHint hint,
                              ProducerType producerType,
                              Instant occurredAt) {
        super(hint, producerType, occurredAt);
        this.action = action;
        this.serviceName = endpoint.getServiceName();
        this.instanceId = endpoint.getInstanceId();
        this.host = endpoint.getHost();
        this.port = endpoint.getPort();
        this.protocol = endpoint.getProtocol();
        this.healthPath = endpoint.getHealthPath();
        this.reason = reason;
        this.registryVersion = registryVersion;
    }
}
