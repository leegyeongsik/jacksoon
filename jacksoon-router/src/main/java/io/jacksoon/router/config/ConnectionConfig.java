package io.jacksoon.router.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jacksoon.common.produce.worker.FileStore;
import io.jacksoon.common.util.HttpRequestCheck;
import io.jacksoon.common.util.HttpResponseCheck;
import io.jacksoon.init.annotation.Init;

import java.nio.file.Path;

@Init
public class ConnectionConfig {
    @Init
    public HttpRequestCheck httpRequestCheck() {
        return new HttpRequestCheck();
    }

    @Init
    public HttpResponseCheck httpResponseCheck() {
        return new HttpResponseCheck();
    }
    @Init
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Init
    public FileStore fileStore(ObjectMapper objectMapper, RouterProperties properties){
        return new FileStore(objectMapper, Path.of(properties.metric().directory()));
    }
}
