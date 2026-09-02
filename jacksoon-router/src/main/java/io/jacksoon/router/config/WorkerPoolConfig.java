package io.jacksoon.router.config;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.FileStore;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPoolManager;
import io.jacksoon.router.connection.RegistryCheckManager;
import io.jacksoon.router.connection.client.ClientConnectionManager;
import io.jacksoon.router.connection.client.ClientConnectionPolicy;
import io.jacksoon.router.connection.client.ClientConnectionTier;
import io.jacksoon.router.filter.FilterExecutor;
import io.jacksoon.router.filter.FilterRequestSetting;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.RouterPipelineTaskExecutor;
import io.jacksoon.router.pipeline.executor.router.HttpReRouter;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.dto.FilterMetricProduceDto;
import io.jacksoon.router.produce.dto.RouterMetricProduceDto;
import io.jacksoon.router.produce.dto.ServiceRequest;
import io.jacksoon.router.produce.metric.ServiceMetricStore;
import io.jacksoon.router.worker.*;

@Init
public class WorkerPoolConfig {
    @Init
    public CommonWorkerPool<RouterPipelineWorker> routerPipelineWorkerPool(RouterPipelineTaskExecutor executor, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(4, () -> new RouterPipelineWorker(routerPipelineQueue, executor, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool(RegistryCheckManager registryCheckManager, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new RegistryCheckWorker(registryCheckManager, 3000L, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool(BackendConnectionPoolManager backendConnectionPoolManager, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ConnectionReduceCheckWorker(backendConnectionPoolManager, 5000L, exceptionDispatcher));
    }

    @Init("coldClientConnectionMonitorPool")
    public CommonWorkerPool<ClientConnectionMonitorWorker> coldClientConnectionMonitorPool(ClientConnectionManager connectionManager, ClientConnectionPolicy connectionPolicy, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ClientConnectionMonitorWorker(connectionManager, connectionPolicy, ClientConnectionTier.COLD, exceptionDispatcher));
    }

    @Init("warmClientConnectionMonitorPool")
    public CommonWorkerPool<ClientConnectionMonitorWorker> warmClientConnectionMonitorPool(ClientConnectionManager connectionManager, ClientConnectionPolicy connectionPolicy, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ClientConnectionMonitorWorker(connectionManager, connectionPolicy, ClientConnectionTier.WARM, exceptionDispatcher));
    }

    @Init("hotClientConnectionMonitorPool")
    public CommonWorkerPool<ClientConnectionMonitorWorker> hotClientConnectionMonitorPool(ClientConnectionManager connectionManager, ClientConnectionPolicy connectionPolicy, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ClientConnectionMonitorWorker(connectionManager, connectionPolicy, ClientConnectionTier.HOT, exceptionDispatcher));
    }

    @Init("clientConnectionClosePool")
    public CommonWorkerPool<ClientConnectionCloseWorker> clientConnectionClosePool(ClientConnectionManager connectionManager, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ClientConnectionCloseWorker(connectionManager, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<FilterWorker> filterWorkerPool(FilterExecutor filterExecutor, FilterRequestSetting filterRequestSetting, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new FilterWorker(filterExecutor, filterRequestSetting, 60_000L, exceptionDispatcher));
    }

    @Init
    public CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool(CommonBlockingQueue<ProduceDto> queue, FileStore fileStore, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(5, () -> new ProduceWorker<>(queue, fileStore, exceptionDispatcher));
    }

    @Init("serviceMetricPool")
    public CommonWorkerPool<ProduceMetricWorker> produceMetricPool(@Init("serviceMetricStore") ServiceMetricStore serviceMetricStore, CommonBlockingQueue<ProduceDto> produceDtoQueue, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ProduceMetricWorker(serviceMetricStore,produceDtoQueue, RouterMetricProduceDto.class, exceptionDispatcher,10000L));
    }

    @Init("filterMetricPool")
    public CommonWorkerPool<ProduceMetricWorker> filterMetricPool(@Init("filterMetricStore")ServiceMetricStore serviceMetricStore, CommonBlockingQueue<ProduceDto> produceDtoQueue, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ProduceMetricWorker(serviceMetricStore,produceDtoQueue, FilterMetricProduceDto.class, exceptionDispatcher,10000L));
    }

    @Init
    public CommonWorkerPool<ReRoutingWorker> reRoutingPool(CommonBlockingQueue<ReRoutingContext> reRoutingQueue, HttpReRouter httpReRouter, ExceptionDispatcher exceptionDispatcher) {
        return new CommonWorkerPool<>(1, () -> new ReRoutingWorker(reRoutingQueue, httpReRouter, exceptionDispatcher));
    }
}
