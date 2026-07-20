package io.jacksoon.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.init.annotation.Init;

@Init
public class CommonConfig {
    @Init
    public HttpRequestCheck httpRequestCheck() {
        return new HttpRequestCheck();
    }
    @Init
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}