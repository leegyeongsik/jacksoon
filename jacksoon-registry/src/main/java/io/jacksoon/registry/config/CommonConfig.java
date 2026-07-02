package io.jacksoon.registry.config;

import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.init.annotation.Init;

@Init
public class CommonConfig {
    @Init
    public HttpRequestCheck httpRequestCheck() {
        return new HttpRequestCheck();
    }
}