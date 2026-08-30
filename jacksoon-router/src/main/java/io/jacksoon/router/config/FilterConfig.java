package io.jacksoon.router.config;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.filter.FilterRequestSetting;

import java.net.URI;

@Init
public class FilterConfig {
    @Init
    public FilterRequestSetting filterRequestSetting(RouterProperties properties) {
        String baseUrl = properties.filter().baseUrl();
        return new FilterRequestSetting(
                URI.create(baseUrl + "/version"),
                URI.create(baseUrl + "/bundle")
        );
    }
}
