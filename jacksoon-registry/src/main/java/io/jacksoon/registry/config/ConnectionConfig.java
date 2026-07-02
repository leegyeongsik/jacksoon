package io.jacksoon.registry.config;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.handle.EndPointConnectionHandler;

@Init
public class ConnectionConfig {
    @Init
    public ConnectionHandlerRegistry<EndPointConnectionHandler> connectionRegistry() {
        return new ConnectionHandlerRegistry<>();
    }

}
