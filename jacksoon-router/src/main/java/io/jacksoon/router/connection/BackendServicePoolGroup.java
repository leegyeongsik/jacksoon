package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;

import java.util.*;

class BackendServicePoolGroup {
    private final BackendConnectionFactory connectionFactory;
    private final Map<String, BackendConnectionPool> endpointPoolMap = new HashMap<>();

    BackendServicePoolGroup(BackendConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    void sync(String serviceName, List<EndpointSnapshot> endpoints) {
        Set<String> liveEndpointIds = new HashSet<>();

        if (endpoints == null) {
            endpoints = List.of();
        }

        for (EndpointSnapshot endpoint : endpoints) {
            String instanceId = endpoint.getInstanceId();
            liveEndpointIds.add(instanceId);

            BackendConnectionPool pool = endpointPoolMap.get(instanceId);

            if (pool == null) {
                endpointPoolMap.put(instanceId, new BackendConnectionPool(endpoint, connectionFactory));
                continue;
            }

            if (!pool.sameEndpoint(endpoint)) {
                pool.close();
                endpointPoolMap.put(instanceId, new BackendConnectionPool(endpoint, connectionFactory));
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

    BackendConnectionPool select() {
        BackendConnectionPool selected = null;

        for (BackendConnectionPool pool : endpointPoolMap.values()) { // 어차피 헤드만 보면되니까 거기서 헤드가 제일 널널한애 뽑는게 맞지
            if (!pool.available()) {
                continue;
            }

            if (selected == null || pool.load() < selected.load()) {
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

    void close() {
        for (BackendConnectionPool pool : endpointPoolMap.values()) {
            pool.close();
        }

        endpointPoolMap.clear();
    }
}