package io.jacksoon.common.connection;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionRegistry {

    private final Map<String, ConnectionContexts> regiMap =
            new ConcurrentHashMap<>();

    public void put(String key, ConnectionContexts context) {
        regiMap.put(key, context);}

    public ConnectionContexts get(String key) {
        return regiMap.get(key);
    }
}