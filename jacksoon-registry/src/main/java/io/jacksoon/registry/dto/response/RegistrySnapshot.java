package io.jacksoon.registry.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class RegistrySnapshot {
    private final List<ServiceSnapshot> services;
    private final List<RouteRuleSnapshot> rules;

    public RegistrySnapshot(List<ServiceSnapshot> services, List<RouteRuleSnapshot> rules) {
        this.services = services;
        this.rules = rules;
    }
}