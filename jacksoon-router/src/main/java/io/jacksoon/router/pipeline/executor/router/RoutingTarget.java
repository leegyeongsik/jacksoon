package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.connection.BackendServicePoolGroup;
import lombok.Getter;

@Getter
public class RoutingTarget {
    private final BackendServicePoolGroup backendServicePoolGroup;
    private final String backendPath;

    public RoutingTarget(BackendServicePoolGroup backendServicePoolGroup, String backendPath) {
        this.backendServicePoolGroup = backendServicePoolGroup;
        this.backendPath = backendPath;
    }

}