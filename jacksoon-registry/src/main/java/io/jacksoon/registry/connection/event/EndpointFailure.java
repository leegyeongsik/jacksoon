package io.jacksoon.registry.connection.event;

import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.store.RegistryStore;
@Init
public class EndpointFailure implements Executor<EndPointEvent>   {
    private final RegistryStore registryStore;

    public EndpointFailure(RegistryStore registryStore) {
        this.registryStore = registryStore;
    }

    @Override
    public void execute(EndPointEvent endPointEvent) {
        registryStore.removeEndpoint(endPointEvent.getServiceName(),endPointEvent.getInstanceId());
    }
}