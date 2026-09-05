package io.jacksoon.filterManagement;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.filterManagement.store.FilterStoreInitializer;
import io.jacksoon.filterManagement.worker.FilterPipelineWorker;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterManagementApplication {
    private final Reactor filterReactor;
    private final CommonWorkerPool<FilterPipelineWorker> filterWorkerPool;
    private final CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoWorkerPool;
    private final FilterStoreInitializer filterStoreInitializer;
    public FilterManagementApplication(
            @Init("FilterReactor") Reactor filterReactor,
            CommonWorkerPool<FilterPipelineWorker> filterWorkerPool,
            CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoWorkerPool,
            FilterStoreInitializer filterStoreInitializer
    ) {
        this.filterReactor = filterReactor;
        this.filterWorkerPool = filterWorkerPool;
        this.produceDtoWorkerPool = produceDtoWorkerPool;
        this.filterStoreInitializer = filterStoreInitializer;
    }

    public void start() {
        filterStoreInitializer.initialize();
        new Thread(filterReactor, "filter-management-reactor").start();
        filterWorkerPool.start();
        produceDtoWorkerPool.start();
    }
}
