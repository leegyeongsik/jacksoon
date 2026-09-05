package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.ServiceSnapshot;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Init
public class BackendConnectionPoolManager {
    private final BackendConnectionFactory connectionFactory;
    private final Map<String, BackendServicePoolGroup> servicePoolMap = new ConcurrentHashMap<>();

    public BackendConnectionPoolManager(BackendConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public synchronized void sync(RegistrySnapshot snapshot) {
        Set<String> liveServices = new HashSet<>();

        List<ServiceSnapshot> services = snapshot.getServices();
        if (services == null) {
            services = List.of();
        }

        for (ServiceSnapshot service : services) {
            String serviceName = service.getServiceName();
            liveServices.add(serviceName);
            BackendServicePoolGroup group = servicePoolMap.computeIfAbsent(serviceName, ignored -> new BackendServicePoolGroup(connectionFactory));
            group.sync(serviceName, service.getEndpoints());
        }
        removeDeadServices(liveServices);
    }

    private void removeDeadServices(Set<String> liveServices) {
        List<String> remove = new ArrayList<>();
        for (String serviceName : servicePoolMap.keySet()) {
            if (!liveServices.contains(serviceName)) {
                remove.add(serviceName);
            }
        }
        for (String serviceName : remove) {
            BackendServicePoolGroup group = servicePoolMap.remove(serviceName);

            if (group != null) {
                group.close();
            }
        }
    }

    public BackendServicePoolGroup select(String serviceName) {
        return servicePoolMap.get(serviceName);
    }

    public void maintain() {
        for (BackendServicePoolGroup group : servicePoolMap.values()) {
            group.maintain();
        }
    }
}