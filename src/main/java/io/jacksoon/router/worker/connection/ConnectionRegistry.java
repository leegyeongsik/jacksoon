package io.jacksoon.router.worker.connection;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.executor.router.ConnectionContexts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Init
public class ConnectionRegistry {

    private final Map<String, ConnectionContexts> regiMap =
            new ConcurrentHashMap<>();

    public void put(String key, ConnectionContexts context) {
        regiMap.put(key, context);}

    public ConnectionContexts get(String key) {
        return regiMap.get(key);
    }
}