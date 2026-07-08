package io.jacksoon.registry.connection.event;

import lombok.Getter;

@Getter
public class EndPointEvent {
    private final String key;
    private final String serviceName;
    private final String instanceId;
    private final String reason;

    public EndPointEvent(String key, String serviceName, String instanceId, String reason) {
        this.key = key;
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.reason = reason;
    }
}
