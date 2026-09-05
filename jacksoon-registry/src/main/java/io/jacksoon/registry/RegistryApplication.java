package io.jacksoon.registry;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.worker.EndPointEventWorker;
import io.jacksoon.registry.worker.EndPointHealthCheckWorker;
import io.jacksoon.registry.worker.EndpointConnectionWorker;
import io.jacksoon.registry.worker.RegistryPipelineWorker;

@Init
public class RegistryApplication {
    private final RegistryInitializer registryInitializer;
    private final Reactor registryReactor;
    private final Reactor endpointReactor;
    private final CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool;
    private final CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool;
    private final CommonWorkerPool<EndPointHealthCheckWorker>healthCheckWorker;
    private final CommonWorkerPool<EndPointEventWorker>eventWorker;
    private final CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool;
    private final ExceptionDispatcher exceptionDispatcher;

    public RegistryApplication(RegistryInitializer registryInitializer,
                               @Init("registryReactor") Reactor registryReactor,
                               @Init("endpointReactor") Reactor endpointReactor,
                               CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool,
                               CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool,
                               CommonWorkerPool<EndPointHealthCheckWorker> healthCheckWorker,
                               CommonWorkerPool<EndPointEventWorker> eventWorker,
                               CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool, ExceptionDispatcher exceptionDispatcher) {
        this.registryInitializer = registryInitializer;
        this.registryReactor = registryReactor;
        this.endpointReactor = endpointReactor;
        this.registryPipelineWorkerPool = registryPipelineWorkerPool;
        this.endpointConnectionWorkerPool = endpointConnectionWorkerPool;
        this.healthCheckWorker = healthCheckWorker;
        this.eventWorker = eventWorker;
        this.produceWorkerPool = produceWorkerPool;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    public void start() {
        try {
            registryInitializer.initialize();
        }catch (Exception e){
            exceptionDispatcher.dispatch(e);
        }
        new Thread(registryReactor, "registry-reactor").start();
        new Thread(endpointReactor, "endpoint-reactor").start();

        registryPipelineWorkerPool.start();
        endpointConnectionWorkerPool.start();
        healthCheckWorker.start();
        eventWorker.start();
        produceWorkerPool.start();
    }
}