package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handler.BackendIOHandler;

@Init
public class FindRouter {
    private final ConnectionHandlerRegistry<BackendIOHandler> connectionRegistry;

    public FindRouter(ConnectionHandlerRegistry<BackendIOHandler> connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    BackendIOHandler getConnection(HttpRequest httpRequest) {
        String key = httpRequest.getPath();
        if(connectionRegistry.get("a") == null){
            throw new RuntimeException();
        }
        return connectionRegistry.get("a");

    }
}
