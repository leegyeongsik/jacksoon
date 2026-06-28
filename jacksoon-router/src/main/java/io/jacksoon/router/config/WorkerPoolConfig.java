package io.jacksoon.router.config;

import io.jacksoon.common.connection.ConnectionContext;
import io.jacksoon.common.connection.ConnectionManager;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.worker.executor.RouterPipelineTaskExecutor;
import io.jacksoon.router.worker.worker.ConnectionWorker;
import io.jacksoon.router.worker.worker.RouterPipelineWorker;
@Init
public class WorkerPoolConfig {
    private static final int ROUTER_WORKER_COUNT = 1;
    private static final int CONNECTION_WORKER_COUNT = 1;
    @Init
    public CommonWorkerPool<RouterPipelineWorker> routerPipelineWorkerPool(RouterPipelineTaskExecutor executor, CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue) {
        return new CommonWorkerPool<>(
                ROUTER_WORKER_COUNT,
                () -> new RouterPipelineWorker(routerPipelineQueue, executor)
        );
    }

    @Init
    public CommonWorkerPool<ConnectionWorker> connectionWorkerPool(ConnectionManager connectionManager, CommonBlockingQueue<ConnectionContext> connectionContextQueue) {
        return new CommonWorkerPool<>(
                CONNECTION_WORKER_COUNT,
                () -> new ConnectionWorker(connectionManager, connectionContextQueue)
        );
    }
}
