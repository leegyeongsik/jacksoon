package io.jacksoon.registry.config;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.produce.worker.SendStore;
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
    @Init
    public CommonWorkerPool<RegistryPipelineWorker> registryPipelineWorkerPool(RegistryPipelineTaskExecutor executor, CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new RegistryPipelineWorker(registryPipelineQueue, executor, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<EndpointConnectionWorker> endpointConnectionWorkerPool(EndPointConnectionManager connectionManager, CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new EndpointConnectionWorker(endpointConnectionQueue, connectionManager, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<EndPointEventWorker> endPointEventWorkerPool(CommonBlockingQueue<EndPointEvent> endPointEventQueue, EndPointEventRegistry endPointEventRegistry, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new EndPointEventWorker(endPointEventQueue, endPointEventRegistry, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<EndPointHealthCheckWorker> endPointHealthCheckWorkerPool(ConnectionHandlerRegistry<EndPointConnectionHandler> registry, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new EndPointHealthCheckWorker(registry, exceptionDispatcher));
    }
    @Init
    public CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool(CommonBlockingQueue<ProduceDto> queue, SendStore sendStore, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ProduceWorker<>(queue, sendStore, exceptionDispatcher));
    }
}