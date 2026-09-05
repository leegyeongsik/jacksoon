package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPoolManager;
import io.jacksoon.router.connection.BackendServicePoolGroup;
import io.jacksoon.router.connection.store.ResolvedRoute;
import io.jacksoon.router.connection.store.RouterRegistryStore;
import io.jacksoon.router.exception.BackendUnavailableException;
import io.jacksoon.router.exception.RouteNotFoundException;

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
            throw new RouteNotFoundException(path);
        }
        return new RoutingTarget(getServiceGroup(route.serviceName()), route.backendPath());
    }
    public BackendServicePoolGroup getServiceGroup(String serviceName){
        BackendServicePoolGroup group = backendConnectionPoolManager.select(serviceName);
        if (group == null) {
            throw new BackendUnavailableException(serviceName, "No backend group. serviceName=" + serviceName);
        }
        return group;
    }
}