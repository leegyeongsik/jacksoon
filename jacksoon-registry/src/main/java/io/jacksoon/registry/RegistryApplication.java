package io.jacksoon.registry;

import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.worker.EndPointEventWorker;
import io.jacksoon.registry.worker.EndPointHealthCheckWorker;
import io.jacksoon.registry.worker.EndpointConnectionWorker;
import io.jacksoon.registry.worker.RegistryPipelineWorker;

@Init
public class RegistryApplication {
    private final Reactor registryReactor;
    private final Reactor endpointReactor;
    private final CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool;
    private final CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool;
    private final CommonWorkerPool<EndPointHealthCheckWorker>healthCheckWorker;
    private final CommonWorkerPool<EndPointEventWorker>eventWorker;


    public RegistryApplication(@Init("registryReactor") Reactor registryReactor, @Init("endpointReactor") Reactor endpointReactor, CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool, CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool, CommonWorkerPool<EndPointHealthCheckWorker> healthCheckWorker, CommonWorkerPool<EndPointEventWorker> eventWorker) {
        this.registryReactor = registryReactor;
        this.endpointReactor = endpointReactor;
        this.registryPipelineWorkerPool = registryPipelineWorkerPool;
        this.endpointConnectionWorkerPool = endpointConnectionWorkerPool;
        this.healthCheckWorker = healthCheckWorker;
        this.eventWorker = eventWorker;
    }

    public void start() {
        new Thread(registryReactor, "registry-reactor").start();
        new Thread(endpointReactor, "endpoint-reactor").start();

        registryPipelineWorkerPool.start();
        endpointConnectionWorkerPool.start();
        healthCheckWorker.start();
        eventWorker.start();

    }
}