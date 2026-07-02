package io.jacksoon.registry.store.entity;

import lombok.Getter;

@Getter
public class RegisteredEndpoint {
    private final String serviceName;
    private final String instanceId;
    private final String host;
    private final int port;
    private final String protocol;
    private final String healthPath;
    private final int weight;

    public RegisteredEndpoint(String serviceName, String instanceId, String host, int port, String protocol, String healthPath, int weight) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
        this.weight = weight;
    }
}