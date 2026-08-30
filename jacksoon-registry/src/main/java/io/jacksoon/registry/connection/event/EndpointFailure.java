package io.jacksoon.registry.connection.event;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.EndPointConnectionHandler;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class EndpointFailure implements Executor<EndPointEvent> {
    private final RegistryStore registryStore;
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry;

    public EndpointFailure(RegistryStore registryStore, ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry) {
        this.registryStore = registryStore;
        this.connectionHandlerRegistry = connectionHandlerRegistry;
    }

    @Override
    public void execute(EndPointEvent endPointEvent) {
        EndPointConnectionHandler current = connectionHandlerRegistry.get(endPointEvent.getKey());
        if (current != null && current != endPointEvent.getHandler()) {
            return;
        }
        registryStore.removeEndpoint(endPointEvent.getServiceName(), endPointEvent.getInstanceId(), endPointEvent.getRegistrationId());
    }
}
