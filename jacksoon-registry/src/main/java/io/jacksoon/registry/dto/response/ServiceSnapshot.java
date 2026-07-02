package io.jacksoon.registry.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ServiceSnapshot {
    private final String serviceName;
    private final List<EndpointSnapshot> endpoints;

    public ServiceSnapshot(String serviceName, List<EndpointSnapshot> endpoints) {
        this.serviceName = serviceName;
        this.endpoints = endpoints;
    }
}