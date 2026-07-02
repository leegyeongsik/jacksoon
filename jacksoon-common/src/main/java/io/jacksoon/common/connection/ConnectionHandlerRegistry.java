package io.jacksoon.common.connection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandlerRegistry<T extends AutoCloseable> { // 따로 레지스트리 관리하는 워커가 존재하고 그 워커가 이것도 관리
    private final Map<String, T> handlerMap = new ConcurrentHashMap<>();

    public void put(String key, T handler) {
        handlerMap.put(key, handler);
    }

    public T get(String key) {
        return handlerMap.get(key);
    }

    public T remove(String key) {
        return handlerMap.remove(key);
    }

    public Collection<T> handlers() {
        return handlerMap.values();
    }

    public void removeAndClose(String key) {
        T handler = handlerMap.remove(key);
        if (handler == null) {
            return;
        }

        try {
            handler.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}