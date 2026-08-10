package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPoolManager;
import io.jacksoon.router.connection.BackendServicePoolGroup;
import io.jacksoon.router.connection.store.ResolvedRoute;
import io.jacksoon.router.connection.store.RouterRegistryStore;

@Init
public class FindRouter {
    private final RouterRegistryStore routerRegistryStore;
    private final BackendConnectionPoolManager backendConnectionPoolManager;

    public FindRouter(RouterRegistryStore routerRegistryStore, BackendConnectionPoolManager backendConnectionPoolManager) {
        this.routerRegistryStore = routerRegistryStore;
        this.backendConnectionPoolManager = backendConnectionPoolManager;
    }

    public RoutingTarget find(HttpRequest httpRequest) {
        String path = httpRequest.getPath();

        ResolvedRoute route = routerRegistryStore.resolve(path);

        if (route == null) {
            throw new IllegalStateException("No route matched. path=" + path);
        }

        BackendServicePoolGroup group = backendConnectionPoolManager.select(route.getServiceName());

        if (group == null) {
            throw new IllegalStateException("No backend group. serviceName=" + route.getServiceName());
        }

        return new RoutingTarget(group, route.getBackendPath());
    }
}