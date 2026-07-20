package io.jacksoon.registry.dto.response;

import lombok.Getter;

@Getter
public class EndpointSnapshot {
    private final String instanceId;
    private final String host;
    private final int port;
    private final String protocol;
    private final String healthPath;

    public EndpointSnapshot(String instanceId, String host, int port, String protocol, String healthPath) {
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
    }
}