package io.jacksoon.router;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.selector.SelectorManager;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.worker.*;

@Init
public class RouterApplication {
    private final Reactor clientReactor;
    private final CommonWorkerPool<RouterPipelineWorker> routerWorkerPool;
    private final CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool;
    private final CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool;
    private final CommonWorkerPool<ClientConnectionMonitorWorker> clientConnectionMonitorWorkerPool;
    private final CommonWorkerPool<FilterWorker> filterWorkerPool;
    private final CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoPool;
    private final CommonWorkerPool<ProduceMetricWorker> serviceMetricWorkerPool;
    private final CommonWorkerPool<ProduceMetricWorker> filterMetricWorkerPool;
    private final CommonWorkerPool<ReRoutingWorker> reRoutingPool;
    private final CommonWorkerPool<ClientConnectionMonitorWorker> coldClientConnectionMonitorPool;
    private final CommonWorkerPool<ClientConnectionMonitorWorker> warmClientConnectionMonitorPool;
    private final CommonWorkerPool<ClientConnectionMonitorWorker> hotClientConnectionMonitorPool;
    private final CommonWorkerPool<ClientConnectionCloseWorker> clientConnectionClosePool;
    private final SelectorManager selectorManager;
    public RouterApplication(@Init("clientReactor") Reactor clientReactor,
                             @Init("coldClientConnectionMonitorPool") CommonWorkerPool<ClientConnectionMonitorWorker> coldClientConnectionMonitorPool,
                             @Init("warmClientConnectionMonitorPool") CommonWorkerPool<ClientConnectionMonitorWorker> warmClientConnectionMonitorPool,
                             @Init("hotClientConnectionMonitorPool") CommonWorkerPool<ClientConnectionMonitorWorker> hotClientConnectionMonitorPool,
                             @Init("serviceMetricPool") CommonWorkerPool<ProduceMetricWorker> serviceMetricWorkerPool,
                             @Init("filterMetricPool") CommonWorkerPool<ProduceMetricWorker> filterMetricWorkerPool,
                             CommonWorkerPool<RouterPipelineWorker> routerWorkerPool,
                             CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool,
                             CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool,
                             CommonWorkerPool<ClientConnectionMonitorWorker> clientConnectionMonitorWorkerPool,
                             CommonWorkerPool<FilterWorker> filterWorkerPool,
                             CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoPool,
                             CommonWorkerPool<ReRoutingWorker> reRoutingPool,
                             CommonWorkerPool<ClientConnectionCloseWorker> clientConnectionClosePool, SelectorManager selectorManager) {
        this.clientReactor = clientReactor;
        this.routerWorkerPool = routerWorkerPool;
        this.registryCheckWorkerPool = registryCheckWorkerPool;
        this.connectionReduceCheckWorkerPool = connectionReduceCheckWorkerPool;
        this.clientConnectionMonitorWorkerPool = clientConnectionMonitorWorkerPool;
        this.filterWorkerPool = filterWorkerPool;
        this.produceDtoPool = produceDtoPool;
        this.serviceMetricWorkerPool = serviceMetricWorkerPool;
        this.filterMetricWorkerPool = filterMetricWorkerPool;
        this.reRoutingPool = reRoutingPool;
        this.coldClientConnectionMonitorPool = coldClientConnectionMonitorPool;
        this.warmClientConnectionMonitorPool = warmClientConnectionMonitorPool;
        this.hotClientConnectionMonitorPool = hotClientConnectionMonitorPool;
        this.clientConnectionClosePool = clientConnectionClosePool;
        this.selectorManager = selectorManager;
    }

    public void start() {
        selectorManager.init(2);
        new Thread(clientReactor, "client-reactor").start();

        routerWorkerPool.start();
        registryCheckWorkerPool.start();
        connectionReduceCheckWorkerPool.start();
        clientConnectionMonitorWorkerPool.start();
        filterWorkerPool.start();
        produceDtoPool.start();
        serviceMetricWorkerPool.start();
        filterMetricWorkerPool.start();
        reRoutingPool.start();

        coldClientConnectionMonitorPool.start();
        warmClientConnectionMonitorPool.start();
        hotClientConnectionMonitorPool.start();
        clientConnectionClosePool.start();

    }
}