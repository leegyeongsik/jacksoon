package io.jacksoon.registry.config;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.event.*;
import io.jacksoon.registry.connection.event.util.EndPointEventExecutorWarrap;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

@Init
public class ConnectionConfig {
    @Init
    public ConnectionHandlerRegistry<EndPointConnectionHandler> connectionRegistry() {
        return new ConnectionHandlerRegistry<>();
    }


    @Init
    public EndPointEventExecutorWarrap<EndPointEvent> endPointEventFail(EndpointFailure endpointFailure) {
        return new EndPointEventExecutorWarrap<>("fail", endpointFailure);
    }

    @Init
    public EndPointEventExecutorWarrap<EndPointEvent> endPointEventSuccess(EndpointSuccess endpointSuccess) {
        return new EndPointEventExecutorWarrap<>("success", endpointSuccess);
    }

    @Init
    public EndPointEventExecutorWarrap<EndPointEvent> endPointEventConnectionSuccess(EndPointConnection endPointConnection) {
        return new EndPointEventExecutorWarrap<>("connection", endPointConnection);
    }
}
