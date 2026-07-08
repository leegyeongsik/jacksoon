package io.jacksoon.router;

import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.worker.ConnectionReduceCheckWorker;
import io.jacksoon.router.worker.RegistryCheckWorker;
import io.jacksoon.router.worker.RouterPipelineWorker;

@Init
public class RouterApplication {
    private final Reactor backendReactor;
    private final Reactor clientReactor;
    private final CommonWorkerPool<RouterPipelineWorker> routerWorkerPool;
    private final CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool;
    private final CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool;

    public RouterApplication(@Init("backendReactor") Reactor backendReactor, @Init("clientReactor") Reactor clientReactor, CommonWorkerPool<RouterPipelineWorker> routerWorkerPool, CommonWorkerPool<RegistryCheckWorker> registryCheckWorkerPool, CommonWorkerPool<ConnectionReduceCheckWorker> connectionReduceCheckWorkerPool){        this.backendReactor = backendReactor;
        this.clientReactor = clientReactor;
        this.routerWorkerPool = routerWorkerPool;
        this.registryCheckWorkerPool = registryCheckWorkerPool;
        this.connectionReduceCheckWorkerPool = connectionReduceCheckWorkerPool;
    }

    public void start() {
        new Thread(clientReactor, "client-reactor").start();
        new Thread(backendReactor, "backend-reactor").start();

        routerWorkerPool.start();
        registryCheckWorkerPool.start();
        connectionReduceCheckWorkerPool.start();
    }
}