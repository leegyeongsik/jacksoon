package io.jacksoon.registry.connection.event;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.worker.Executor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.RegistryEventException;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

@Init
public class EndPointConnection implements Executor<EndPointEvent> {
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry;
    public EndPointConnection(ConnectionHandlerRegistry<EndPointConnectionHandler> connectionHandlerRegistry) {
        this.connectionHandlerRegistry = connectionHandlerRegistry;
    }
    @Override
    public void execute(EndPointEvent endPointEvent) {
        if (!(endPointEvent instanceof EndPointConnectionEvent connectionEvent)) {
            throw new RegistryEventException("connection event requires EndPointConnectionEvent");
        }
        EndPointConnectionHandler current = connectionHandlerRegistry.get(connectionEvent.getKey());
        if (current != connectionEvent.getHandler()) {
            return;
        }
        connectionHandlerRegistry.put(connectionEvent.getKey(), connectionEvent.getHandler());
    }
}
