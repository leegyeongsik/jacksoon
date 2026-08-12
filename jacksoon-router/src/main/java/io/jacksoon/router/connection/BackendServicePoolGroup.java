package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;
import io.jacksoon.router.pipeline.context.ProxyContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackendServicePoolGroup {
    private final BackendConnectionFactory connectionFactory;
    private final Map<String, BackendConnectionPool> endpointPoolMap = new HashMap<>();

    BackendServicePoolGroup(BackendConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    synchronized void sync(String serviceName, List<EndpointSnapshot> endpoints) {
        Set<String> liveEndpointIds = new HashSet<>();

        if (endpoints == null) {
            endpoints = List.of();
        }

        for (EndpointSnapshot endpoint : endpoints) {
            String instanceId = endpoint.getInstanceId();
            liveEndpointIds.add(instanceId);

            BackendConnectionPool pool = endpointPoolMap.get(instanceId);
            if (pool == null) { // 풀하나가 인스턴스임 풀안에 커넥션풀
                endpointPoolMap.put(instanceId, new BackendConnectionPool(serviceName, endpoint, connectionFactory));
                continue;
            }
            if (!pool.sameEndpoint(endpoint)) {
                pool.close();
                endpointPoolMap.put(instanceId, new BackendConnectionPool(serviceName, endpoint, connectionFactory));
            }
        }
        removeDeadEndpoints(liveEndpointIds);
    }

    private void removeDeadEndpoints(Set<String> liveEndpointIds) {
        List<String> remove = new ArrayList<>();
        for (String s : endpointPoolMap.keySet()) {
            if (!liveEndpointIds.contains(s)) {
                remove.add(s);
            }
        }
        for (String s : remove) {
            BackendConnectionPool pool = endpointPoolMap.remove(s);
            if (pool != null) {
                pool.close();
            }
        }
    }

    public synchronized void send(ProxyContext context) {
        BackendConnectionPool selected = selectInternal();
        if (selected == null) {
            throw new IllegalStateException("No available backend connection pool");
        }
        selected.send(context);
    }
    private BackendConnectionPool selectInternal() {
        BackendConnectionPool selected = null;
        for (BackendConnectionPool pool : endpointPoolMap.values()) {
            if (!pool.available()) {
                continue;
            }
            if (selected == null || pool.load() < selected.load()) {
                selected = pool;
            }
        }
        return selected;
    }

    synchronized void maintain() {
        for (BackendConnectionPool pool : endpointPoolMap.values()) {
            pool.maintain();
        }
    }

    synchronized void close() {
        for (BackendConnectionPool pool : endpointPoolMap.values()) {
            pool.close();
        }
        endpointPoolMap.clear();
    }
}