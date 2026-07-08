package io.jacksoon.common.registry.dto.response;

public class EndpointSnapshot {
    private String instanceId;
    private String host;
    private int port;
    private String protocol;
    private String healthPath;
    private int weight;

    public EndpointSnapshot() {
    }

    public EndpointSnapshot(String instanceId, String host, int port, String protocol, String healthPath, int weight) {
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
        this.weight = weight;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHealthPath() {
        return healthPath;
    }

    public int getWeight() {
        return weight;
    }
}