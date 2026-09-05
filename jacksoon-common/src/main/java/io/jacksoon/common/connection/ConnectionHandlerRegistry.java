package io.jacksoon.common.connection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandlerRegistry<T extends AutoCloseable> {
    private final Map<String, T> handlerMap = new ConcurrentHashMap<>();

    public T put(String key, T handler) {
        return handlerMap.put(key, handler);
    }

    public T get(String key) {
        return handlerMap.get(key);
    }

    public T remove(String key) {
        return handlerMap.remove(key);
    }

    public boolean remove(String key, T handler) {
        return handlerMap.remove(key, handler);
    }

    public Collection<T> handlers() {
        return handlerMap.values();
    }
}
