package io.jacksoon.router;

import io.jacksoon.common.selector.Reactor;
import io.jacksoon.common.util.CommonWorkerPool;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.worker.ConnectionWorker;
import io.jacksoon.router.worker.RouterPipelineWorker;

@Init
public class RouterApplication {
    private final Reactor backendReactor;
    private final Reactor clientReactor;
    private final CommonWorkerPool<RouterPipelineWorker> routerWorkerPool;
    private final CommonWorkerPool<ConnectionWorker> connectionWorkerPool;
    public RouterApplication(@Init("backendReactor") Reactor backendReactor, @Init("clientReactor") Reactor clientReactor, CommonWorkerPool<RouterPipelineWorker> routerWorkerPool, CommonWorkerPool<ConnectionWorker> connectionWorkerPool) {
        this.backendReactor = backendReactor;
        this.clientReactor = clientReactor;
        this.routerWorkerPool = routerWorkerPool;
        this.connectionWorkerPool = connectionWorkerPool;
    }

    public void start() {
        new Thread(clientReactor, "client-reactor").start();
        new Thread(backendReactor, "backend-reactor").start();
        this.routerWorkerPool.start();
        this.connectionWorkerPool.start();
    }
}