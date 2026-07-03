package io.jacksoon.registry.connection.event;

import io.jacksoon.registry.handle.EndPointConnectionHandler;
import lombok.Getter;
@Getter
public class EndPointConnectionEvent extends EndPointEvent{
    private final EndPointConnectionHandler handler;
    public EndPointConnectionEvent(String key, String serviceName, String instanceId, String reason, EndPointConnectionHandler handler) {
        super(key, serviceName, instanceId, reason);
        this.handler = handler;
    }
}
