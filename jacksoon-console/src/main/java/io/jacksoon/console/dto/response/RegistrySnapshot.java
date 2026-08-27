package io.jacksoon.console.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrySnapshot {
    private long version;
    private List<ServiceSnapshot> services;
    private List<RouteRuleSnapshot> rules;

    public RegistrySnapshot(long version, List<ServiceSnapshot> services, List<RouteRuleSnapshot> rules) {
        this.version = version;
        this.services = services;
        this.rules = rules;
    }
}
