package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;
import io.jacksoon.router.exception.BackendUnavailableException;
import io.jacksoon.router.pipeline.context.ProxyContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BackendServicePoolGroup {
    private final BackendConnectionFactory connectionFactory;
    private final Map<String, BackendConnectionPool> endpointPoolMap = new ConcurrentHashMap<>();
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
        for (String instanceId : endpointPoolMap.keySet()) {
            if (!liveEndpointIds.contains(instanceId)) {
                remove.add(instanceId);
            }
        }
        for (String instanceId : remove) {
            BackendConnectionPool pool = endpointPoolMap.remove(instanceId);
            if (pool != null) {
                pool.close();
            }
        }
    }

    public void send(ProxyContext context) {
        BackendConnectionPool selected = selectInternal();
        if (selected == null) {
            throw new BackendUnavailableException(null, "No available backend connection pool");
        }
        selected.send(context);
    }
    private BackendConnectionPool selectInternal() {
        BackendConnectionPool selected = null;
        for (BackendConnectionPool pool : endpointPoolMap.values()) {
            if (!pool.available()) {
                continue;
            }
            if (selected == null || pool.totalLoad() < selected.totalLoad()) {
                selected = pool;
            }
        }
        return selected;
    }

    void maintain() {
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