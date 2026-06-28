package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.common.connection.ConnectionContexts;
import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.common.connection.ConnectionRegistry;
import io.jacksoon.init.annotation.Init;

@Init
public class FindRouter {
    private final ConnectionRegistry connectionRegistry;

    public FindRouter(ConnectionRegistry connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    ConnectionContexts getConnection(HttpRequest httpRequest) {
        String key = httpRequest.getPath();
        if(connectionRegistry.get("a") == null){
            throw new RuntimeException();
        }
        return connectionRegistry.get("a");

    }
}
