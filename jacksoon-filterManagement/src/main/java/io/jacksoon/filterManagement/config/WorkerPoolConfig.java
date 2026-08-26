package io.jacksoon.filterManagement.config;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.worker.ProduceWorker;
import io.jacksoon.common.produce.worker.SendStore;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.executor.FilterPipelineTaskExecutor;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.filterManagement.worker.FilterPipelineWorker;
import io.jacksoon.init.annotation.Init;

@Init
public class WorkerPoolConfig {
    @Init
    public CommonWorkerPool<FilterPipelineWorker> filterPipelineWorkerPool(FilterPipelineTaskExecutor executor, CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue, FilterStore filterStore) {
        return new CommonWorkerPool<>(1, () -> new FilterPipelineWorker(filterPipelineQueue, executor, filterStore));
    }
    @Init
    public CommonWorkerPool<ProduceWorker<ProduceDto>> produceWorkerPool(CommonBlockingQueue<ProduceDto> queue, SendStore sendStore) {
        return new CommonWorkerPool<>(1, () -> new ProduceWorker<>(queue, sendStore));
    }
}
