package io.jacksoon.registry.connection.event;

import io.jacksoon.registry.handle.EndPointConnectionHandler;
import lombok.Getter;

@Getter
public class EndPointEvent {
    private final String key;
    private final String serviceName;
    private final String instanceId;
    private final String reason;
    private final long registrationId;
    private final EndPointConnectionHandler handler;

    public EndPointEvent(String key, String serviceName, String instanceId, String reason, long registrationId, EndPointConnectionHandler handler) {
        this.key = key;
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.reason = reason;
        this.registrationId = registrationId;
        this.handler = handler;
    }
}
