package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.connection.BackendConnectionPool;

public class RoutingTarget {
    private final BackendConnectionPool pool;
    private final String backendPath;

    public RoutingTarget(BackendConnectionPool pool, String backendPath) {
        this.pool = pool;
        this.backendPath = backendPath;
    }

    public BackendConnectionPool getPool() {
        return pool;
    }

    public String getBackendPath() {
        return backendPath;
    }
}