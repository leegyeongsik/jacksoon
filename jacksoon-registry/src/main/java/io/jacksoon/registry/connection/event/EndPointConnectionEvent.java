package io.jacksoon.registry.connection.event;

import io.jacksoon.registry.handle.EndPointConnectionHandler;

public class EndPointConnectionEvent extends EndPointEvent {
    public EndPointConnectionEvent(String key, String serviceName, String instanceId, String reason, long registrationId, EndPointConnectionHandler handler) {
        super(key, serviceName, instanceId, reason, registrationId, handler);
    }
}
