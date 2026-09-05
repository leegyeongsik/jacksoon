package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.connection.BackendServicePoolGroup;

public record RoutingTarget(BackendServicePoolGroup backendServicePoolGroup, String backendPath) {

}