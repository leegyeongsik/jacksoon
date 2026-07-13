package io.jacksoon.router.filter;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.common.filter.RouterFilter;

public record RegisteredFilter(
        FilterConfigDto config,
        RouterFilter filter
) {
    public boolean matches(String requestPath) {
        String configuredPath = config.path();
        if (configuredPath == null || configuredPath.isBlank()) {
            return true;
        }
        if (requestPath == null) {
            return false;
        }
        return requestPath.equals(configuredPath) || requestPath.startsWith(configuredPath + "/");
    }}
