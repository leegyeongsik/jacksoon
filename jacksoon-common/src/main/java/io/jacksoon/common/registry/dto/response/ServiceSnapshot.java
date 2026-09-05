package io.jacksoon.common.registry.dto.response;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Setter
@NoArgsConstructor
public class ServiceSnapshot {
    private String serviceName;
    private List<EndpointSnapshot> endpoints;
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