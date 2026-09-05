package io.jacksoon.console.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EndpointSnapshot {
    String instanceId;
    String host;
    int port;
    String protocol;
    String healthPath;
    public EndpointSnapshot(String instanceId, String host, int port, String protocol, String healthPath) {
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
    }
}