package io.jacksoon.filterManagement;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.filterManagement.worker.FilterPipelineWorker;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterManagementApplication {
    private final Reactor filterReactor;
    private final CommonWorkerPool<FilterPipelineWorker> filterWorkerPool;
    private final CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoWorkerPool;
    public FilterManagementApplication(@Init("FilterReactor") Reactor filterReactor, CommonWorkerPool<FilterPipelineWorker> filterWorkerPool, CommonWorkerPool<ProduceWorker<ProduceDto>> produceDtoWorkerPool) {
        this.filterReactor = filterReactor;
        this.filterWorkerPool = filterWorkerPool;
        this.produceDtoWorkerPool = produceDtoWorkerPool;
    }

    public void start() {
        new Thread(filterReactor, "filter-management-reactor").start();
        filterWorkerPool.start();
        produceDtoWorkerPool.start();
    }
}
