package io.jacksoon.registry.dto.response;

import lombok.Getter;

@Getter
public class RouteRuleSnapshot {
    private final String serviceName;
    private final String pathPrefix;
    private final boolean stripPrefix;

    public RouteRuleSnapshot(String serviceName, String pathPrefix, boolean stripPrefix) {
        this.serviceName = serviceName;
        this.pathPrefix = pathPrefix;
        this.stripPrefix = stripPrefix;
    }
}