package io.jacksoon.common.registry.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteRuleSnapshot {
    private String serviceName;
    private String pathPrefix;
    private boolean stripPrefix;


    public RouteRuleSnapshot(String serviceName, String pathPrefix, boolean stripPrefix) {
        this.serviceName = serviceName;
        this.pathPrefix = pathPrefix;
        this.stripPrefix = stripPrefix;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public boolean isStripPrefix() {
        return stripPrefix;
    }
}