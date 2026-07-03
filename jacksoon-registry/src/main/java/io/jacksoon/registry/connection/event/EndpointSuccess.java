package io.jacksoon.registry.connection.event;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.store.RegistryStore;
@Init
public class EndpointSuccess implements Executor<EndPointEvent> {
    private final RegistryStore registryStore;
    public EndpointSuccess(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }
    @Override
    public void execute(EndPointEvent endPointEvent) {
        registryStore.successEndpoint(endPointEvent.getServiceName(), endPointEvent.getInstanceId());
    }
}
