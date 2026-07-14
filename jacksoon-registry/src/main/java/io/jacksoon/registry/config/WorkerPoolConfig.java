package io.jacksoon.registry.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.EndPointConnectionManager;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.connection.event.EndPointEventRegistry;
import io.jacksoon.registry.handle.EndPointConnectionHandler;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.executor.RegistryPipelineTaskExecutor;
import io.jacksoon.registry.worker.EndPointEventWorker;
import io.jacksoon.registry.worker.EndPointHealthCheckWorker;
import io.jacksoon.registry.worker.EndpointConnectionWorker;
import io.jacksoon.registry.worker.RegistryPipelineWorker;

@Init
public class WorkerPoolConfig {
    private final String PATH = "localhost:1014/create";
    @Init
    public CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool(RegistryPipelineTaskExecutor executor, CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue) {
        return new CommonWorkerPool<>(1, () -> new RegistryPipelineWorker(registryPipelineQueue, executor));
    }

    @Init
    public CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool(EndPointConnectionManager connectionManager, CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue) {
        return new CommonWorkerPool<>(1, () -> new EndpointConnectionWorker(endpointConnectionQueue, connectionManager));
    }

    @Init
    public CommonWorkerPool<EndPointEventWorker> endPointEventWorkerPool(CommonBlockingQueue<EndPointEvent> endPointEventQueue , EndPointEventRegistry endPointEventRegistry) {
        return new CommonWorkerPool<>(1, () -> new EndPointEventWorker(endPointEventQueue,endPointEventRegistry));
    }

    @Init
    public CommonWorkerPool<EndPointHealthCheckWorker> endPointHealthCheckWorkerPool(ConnectionHandlerRegistry<EndPointConnectionHandler> registry) {
        return new CommonWorkerPool<>(1, () -> new EndPointHealthCheckWorker(registry));
    }
    @Init
    public CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool(CommonBlockingQueue<ProduceDto> queue, ObjectMapper objectMapper) {
        return new CommonWorkerPool<>(1, () -> new ProduceWorker<>(queue, PATH, objectMapper));
    }

}