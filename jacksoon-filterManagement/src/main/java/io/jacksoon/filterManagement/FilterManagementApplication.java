package io.jacksoon.filterManagement;

import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.filterManagement.worker.FilterPipelineWorker;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterManagementApplication {
    private final Reactor filterReactor;
    private final CommonWorkerPool<FilterPipelineWorker> filterWorkerPool;

    public FilterManagementApplication(
            @Init("FilterReactor") Reactor filterReactor,
            CommonWorkerPool<FilterPipelineWorker> filterWorkerPool
    ) {
        this.filterReactor = filterReactor;
        this.filterWorkerPool = filterWorkerPool;
    }

    public void start() {
        new Thread(filterReactor, "filter-management-reactor").start();
        filterWorkerPool.start();
    }
}
