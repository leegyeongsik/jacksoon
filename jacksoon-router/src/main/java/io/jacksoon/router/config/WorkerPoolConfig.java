package io.jacksoon.router.config;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.FileStore;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPoolManager;
import io.jacksoon.router.connection.RegistryCheckManager;
import io.jacksoon.router.filter.FilterExecutor;
import io.jacksoon.router.filter.FilterRequestSetting;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.RouterPipelineTaskExecutor;
import io.jacksoon.router.pipeline.executor.router.HttpReRouter;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.dto.FilterMetricProduceDto;
import io.jacksoon.router.produce.dto.RouterMetricProduceDto;
import io.jacksoon.router.produce.dto.ServiceRequest;
import io.jacksoon.router.worker.*;

@Init
public class WorkerPoolConfig {
    @Init
    public CommonWorkerPool<RouterPipelineWorker> routerPipelineWorkerPool(RouterPipelineTaskExecutor executor, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue) {
        return new CommonWorkerPool<>(4, () -> new RouterPipelineWorker(routerPipelineQueue, executor));
    }

    @Init
    public CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool(RegistryCheckManager registryCheckManager) {
        return new CommonWorkerPool<>(1, () -> new RegistryCheckWorker(registryCheckManager, 3000L));
    }

    @Init
    public CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool(BackendConnectionPoolManager backendConnectionPoolManager) {
        return new CommonWorkerPool<>(1, () -> new ConnectionReduceCheckWorker(backendConnectionPoolManager, 5000L));
    }

    @Init
    public CommonWorkerPool<FilterWorker> filterWorkerPool(FilterExecutor filterExecutor, FilterRequestSetting filterRequestSetting) {
        return new CommonWorkerPool<>(1, () -> new FilterWorker(filterExecutor, filterRequestSetting, 60_000L));
    }

    @Init
    public CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool(CommonBlockingQueue<ProduceDto> queue, FileStore fileStore) {
        return new CommonWorkerPool<>(5, () -> new ProduceWorker<>(queue, fileStore));
    }
    @Init("serviceMetricPool")
    public CommonWorkerPool<ProduceMetricWorker> produceMetricPool(@Init("serviceMetricQueue") CommonBlockingQueue<ServiceRequest> serviceRequestQueue,CommonBlockingQueue<ProduceDto> produceDtoQueue){
        return new CommonWorkerPool<>(5,()->new ProduceMetricWorker(serviceRequestQueue,produceDtoQueue, RouterMetricProduceDto.class));
    }
    @Init("filterMetricPool")
    public CommonWorkerPool<ProduceMetricWorker> filterMetricPool(@Init("filterMetricQueue") CommonBlockingQueue<ServiceRequest> serviceRequestQueue,CommonBlockingQueue<ProduceDto> produceDtoQueue){
        return new CommonWorkerPool<>(1,()->new ProduceMetricWorker(serviceRequestQueue,produceDtoQueue, FilterMetricProduceDto.class));
    }
    @Init
    public CommonWorkerPool<ReRoutingWorker> reRoutingPool(CommonBlockingQueue<ReRoutingContext> reRoutingQueue , HttpReRouter httpReRouter){
        return new CommonWorkerPool<>(1,()->new ReRoutingWorker(reRoutingQueue,httpReRouter));
    }
}
