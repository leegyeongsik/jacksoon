package io.jacksoon.router.config;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.connection.BackendConnectionPoolManager;
import io.jacksoon.router.connection.RegistryCheckManager;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.RouterPipelineTaskExecutor;
import io.jacksoon.router.worker.ConnectionReduceCheckWorker;
import io.jacksoon.router.worker.RegistryCheckWorker;
import io.jacksoon.router.worker.RouterPipelineWorker;
@Init
public class WorkerPoolConfig {
    @Init
    public CommonWorkerPool<RouterPipelineWorker> routerPipelineWorkerPool(RouterPipelineTaskExecutor executor, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue) {
        return new CommonWorkerPool<>(
                1,
                () -> new RouterPipelineWorker(routerPipelineQueue, executor)
        );
    }
    @Init
    public CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool(RegistryCheckManager registryCheckManager) {
        return new CommonWorkerPool<>(
                1,
                () -> new RegistryCheckWorker(registryCheckManager, 3000L)
        );
    }

    @Init
    public CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool(BackendConnectionPoolManager backendConnectionPoolManager) {
        return new CommonWorkerPool<>(
                1,
                () -> new ConnectionReduceCheckWorker(backendConnectionPoolManager, 5000L)
        );
    }
}
