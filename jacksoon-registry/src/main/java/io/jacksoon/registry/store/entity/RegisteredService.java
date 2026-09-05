package io.jacksoon.registry.store.entity;

import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class RegisteredService {
    private final String serviceName;
    private final Map<String, RegisteredEndpoint> endpointMap = new ConcurrentHashMap<>();

    public RegisteredService(String serviceName) {
        this.serviceName = serviceName;
    }

    public RegisteredEndpoint putEndpoint(RegisteredEndpoint endpoint) {
        return endpointMap.put(endpoint.getInstanceId(), endpoint);
    }

    public RegisteredEndpoint getEndpoint(String instanceId) {
        return endpointMap.get(instanceId);
    }

    public Collection<RegisteredEndpoint> endpoints() {
        return endpointMap.values();
    }

    public RegisteredEndpoint removeEndpoint(String instanceId) {
        return endpointMap.remove(instanceId);
    }
}
