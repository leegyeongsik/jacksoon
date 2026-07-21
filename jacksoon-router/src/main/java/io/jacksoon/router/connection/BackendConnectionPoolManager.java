package io.jacksoon.router.connection;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.ServiceSnapshot;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.factory.BackendConnectionFactory;

import java.util.*;

@Init
public class BackendConnectionPoolManager {
    private final BackendConnectionFactory connectionFactory;
    private final Map<String, BackendServicePoolGroup> servicePoolMap = new HashMap<>();

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
        for (String s : servicePoolMap.keySet()) {
            if (!liveServices.contains(s)) {
                remove.add(s);
            }
        }
        for (String s : remove) {
            BackendServicePoolGroup backendServicePoolGroup = servicePoolMap.remove(s);
            if (backendServicePoolGroup != null) {
                backendServicePoolGroup.close();
            }
        }
    }

    public synchronized BackendServicePoolGroup select(String serviceName) {
        BackendServicePoolGroup group = servicePoolMap.get(serviceName);
        if (group == null) {
            return null;
        }
        return group;
    }

    public synchronized void maintain() {
        for (BackendServicePoolGroup group : servicePoolMap.values()) {
            group.maintain();
        }
    }
}