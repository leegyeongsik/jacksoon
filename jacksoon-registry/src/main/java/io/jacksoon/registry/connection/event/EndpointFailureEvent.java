package io.jacksoon.registry.connection.event;

import lombok.Getter;

@Getter
public class EndpointFailureEvent extends EndPointEvent{
    public EndpointFailureEvent(String key, String serviceName, String instanceId, String reason) {
        super(key, serviceName, instanceId, reason);
    }
}