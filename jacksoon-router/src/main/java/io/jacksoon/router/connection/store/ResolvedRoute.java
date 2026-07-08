package io.jacksoon.router.connection.store;

public class ResolvedRoute {
    private final String serviceName;
    private final String backendPath;

    public ResolvedRoute(String serviceName, String backendPath) {
        this.serviceName = serviceName;
        this.backendPath = backendPath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getBackendPath() {
        return backendPath;
    }
}