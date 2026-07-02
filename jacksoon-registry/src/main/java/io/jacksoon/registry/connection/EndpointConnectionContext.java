package io.jacksoon.registry.connection;

import io.jacksoon.common.connection.ConnectionContext;
import lombok.Getter;

@Getter
public class EndpointConnectionContext extends ConnectionContext {
    private final String serviceName;
    private final String instanceId;
    private final String host;
    private final int port;
    private final String protocol;
    private final String healthPath;

    public EndpointConnectionContext(String serviceName, String instanceId, String host, int port, String protocol, String healthPath) {
        super(host,port);
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
    }

    public String key() {
        return serviceName + ":" + instanceId;
    }
}