package io.jacksoon.registry;

import io.jacksoon.common.registry.dto.response.EndpointSnapshot;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.ServiceSnapshot;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.connection.store.ConsoleRegistryClient;
import io.jacksoon.registry.store.RegistryStore;

import java.util.List;

@Init
public class RegistryInitializer {
    private final ConsoleRegistryClient consoleRegistryClient;
    private final RegistryStore registryStore;
    private final CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue;

    public RegistryInitializer(ConsoleRegistryClient consoleRegistryClient, RegistryStore registryStore, CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue) {
        this.consoleRegistryClient = consoleRegistryClient;
        this.registryStore = registryStore;
        this.endpointConnectionQueue = endpointConnectionQueue;
    }
    public void initialize() {
        RegistrySnapshot snapshot = consoleRegistryClient.snapshot();
        registryStore.initialize(snapshot);
        List<ServiceSnapshot> services = snapshot.getServices() == null ? List.of() : snapshot.getServices();
        for (ServiceSnapshot service : services) {
            List<EndpointSnapshot> endpoints = service.getEndpoints() == null ? List.of() : service.getEndpoints();
            for (EndpointSnapshot endpoint : endpoints) {
                endpointConnectionQueue.put(new EndpointConnectionContext(
                        service.getServiceName(),
                        endpoint.getInstanceId(),
                        endpoint.getHost(),
                        endpoint.getPort(),
                        endpoint.getProtocol(),
                        endpoint.getHealthPath()
                ));
            }
        }
    }
}
