package io.jacksoon.registry.connection.event;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.EndPointConnectionHandler;
import io.jacksoon.registry.store.RegistryStore;

@Init
public class EndpointSuccess implements Executor<EndPointEvent> {
    private final RegistryStore registryStore;
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry;

    public EndpointSuccess(RegistryStore registryStore, ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry) {
        this.registryStore = registryStore;
        this.connectionHandlerRegistry = connectionHandlerRegistry;
    }
    @Override
    public void execute(EndPointEvent endPointEvent) {
        if (connectionHandlerRegistry.get(endPointEvent.getKey()) != endPointEvent.getHandler()) {
            return;
        }
        registryStore.successEndpoint(endPointEvent.getServiceName(), endPointEvent.getInstanceId(), endPointEvent.getRegistrationId());
    }
}
