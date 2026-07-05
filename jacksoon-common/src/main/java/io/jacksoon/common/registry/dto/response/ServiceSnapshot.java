package io.jacksoon.common.registry.dto.response;

import java.util.List;

public class ServiceSnapshot {
    private String serviceName;
    private List<EndpointSnapshot> endpoints;

    public ServiceSnapshot() {
    }

    public ServiceSnapshot(String serviceName, List<EndpointSnapshot> endpoints) {
        this.serviceName = serviceName;
        this.endpoints = endpoints;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<EndpointSnapshot> getEndpoints() {
        return endpoints;
    }
}