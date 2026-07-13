package io.jacksoon.filterManagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.init.annotation.Init;

@Init
public class CommonConfig {
    @Init
    public HttpRequestCheck httpRequestCheck() {
        return new HttpRequestCheck();
    }

    @Init
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
