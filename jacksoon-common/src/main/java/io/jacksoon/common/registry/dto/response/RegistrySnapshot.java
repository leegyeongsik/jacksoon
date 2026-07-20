package io.jacksoon.common.registry.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class RegistrySnapshot {
    private List<ServiceSnapshot> services;
    private List<RouteRuleSnapshot> rules;
    public RegistrySnapshot(List<ServiceSnapshot> services, List<RouteRuleSnapshot> rules) {
        this.services = services;
        this.rules = rules;
    }

    public List<ServiceSnapshot> getServices() {
        return services;
    }

    public void setServices(List<ServiceSnapshot> services) {
        this.services = services;
    }

    public List<RouteRuleSnapshot> getRules() {
        return rules;
    }

    public void setRules(List<RouteRuleSnapshot> rules) {
        this.rules = rules;
    }
}