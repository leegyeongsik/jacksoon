package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.handle.Handler;
import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterRequest;
import io.jacksoon.router.worker.connection.ConnectionContext;
import io.jacksoon.router.worker.connection.ConnectionRegistry;

import javax.print.DocFlavor;
import java.net.http.HttpRequest;

@Init
public class FindRouter {
    private final ConnectionRegistry connectionRegistry;

    public FindRouter(ConnectionRegistry connectionRegistry) {
        this.connectionRegistry = connectionRegistry;
    }

    ConnectionContexts getConnection(RouterRequest routerRequest) {
        String key = routerRequest.getPath();
        if(connectionRegistry.get("a") == null){
            throw new RuntimeException();
        }
        return connectionRegistry.get("a");

    }
}
