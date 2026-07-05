package io.jacksoon.common.connection;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandlerRegistry<T extends AutoCloseable> {
    private final Map<String, T> handlerMap = new ConcurrentHashMap<>(); // 그러면 저거를 집어넣으면되지
    // 일단 그러면 이거를 수정하면 service안에 맵이 있는식으로 인스턴스로다가

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