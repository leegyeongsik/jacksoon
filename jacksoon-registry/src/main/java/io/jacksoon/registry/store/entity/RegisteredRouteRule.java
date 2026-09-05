package io.jacksoon.registry.store.entity;

import lombok.Getter;

import java.util.List;
@Getter
public class RegisteredRouteRule {
    private final String serviceName;
    private final String pathPrefix;
    private final boolean stripPrefix;

    public RegisteredRouteRule(String serviceName, String pathPrefix, boolean stripPrefix) {
        this.serviceName = serviceName;
        this.pathPrefix = pathPrefix;
        this.stripPrefix = stripPrefix;
    }
}