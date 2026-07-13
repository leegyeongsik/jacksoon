package io.jacksoon.router.config;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.filter.FilterRequestSetting;

import java.net.URI;

@Init
public class FilterConfig {
    @Init
    public FilterRequestSetting filterRequestSetting() {
        return new FilterRequestSetting(
                URI.create("http://127.0.0.1:1011/version"),
                URI.create("http://127.0.0.1:1011/bundle")
        );
    }
}
