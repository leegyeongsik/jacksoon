package io.jacksoon.router;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.worker.*;

@Init
public class RouterApplication {
    private final Reactor backendReactor;
    private final Reactor clientReactor;
    private final CommonWorkerPool<RouterPipelineWorker> routerWorkerPool;
    private final CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool;
    private final CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool;
    private final CommonWorkerPool<FilterWorker> filterWorkerPool;
    private final CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoPool;
    private final CommonWorkerPool<ProduceMetricWorker> metricWorkerPool;
    public RouterApplication(@Init("backendReactor") Reactor backendReactor, @Init("clientReactor") Reactor clientReactor, CommonWorkerPool<RouterPipelineWorker> routerWorkerPool, CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool, CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool, CommonWorkerPool<FilterWorker> filterWorkerPool, CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoPool, CommonWorkerPool<ProduceMetricWorker> metricWorkerPool){        this.backendReactor = backendReactor;
        this.clientReactor = clientReactor;
        this.routerWorkerPool = routerWorkerPool;
        this.registryCheckWorkerPool = registryCheckWorkerPool;
        this.connectionReduceCheckWorkerPool = connectionReduceCheckWorkerPool;
        this.filterWorkerPool = filterWorkerPool;
        this.produceDtoPool = produceDtoPool;
        this.metricWorkerPool = metricWorkerPool;
    }

    public void start() {
        new Thread(clientReactor, "client-reactor").start();
        new Thread(backendReactor, "backend-reactor").start();

        routerWorkerPool.start();
        registryCheckWorkerPool.start();
        connectionReduceCheckWorkerPool.start();
        filterWorkerPool.start();
        produceDtoPool.start();
        metricWorkerPool.start();
    }
}