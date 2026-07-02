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

    public void putEndpoint(RegisteredEndpoint endpoint) {
        endpointMap.put(endpoint.getInstanceId(), endpoint);
    }
    public Collection<RegisteredEndpoint> endpoints() {
        return endpointMap.values();
    }
    public void removeEndpoint(String instanceId) {
        endpointMap.remove(instanceId);
    }
}