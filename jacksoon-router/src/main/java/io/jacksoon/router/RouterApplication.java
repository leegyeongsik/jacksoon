package io.jacksoon.router;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.connection.ConnectionWorkerPool;
import io.jacksoon.router.worker.thread.RequestWorkerPool;

@Init
public class RouterApplication {

    private final Reactor backendReactor;
    private final Reactor clientReactor;
    private final RequestWorkerPool requestWorkerPool;
    private final ConnectionWorkerPool connectionWorkerPool;

    public RouterApplication(@Init("backendReactor") Reactor backendReactor, @Init("clientReactor")Reactor clientReactor, RequestWorkerPool requestWorkerPool, ConnectionWorkerPool connectionWorkerPool) {
        this.backendReactor = backendReactor;
        this.clientReactor = clientReactor;
        this.requestWorkerPool = requestWorkerPool;
        this.connectionWorkerPool = connectionWorkerPool;
    }

    public void start() {
        new Thread(clientReactor, "client-reactor").start();
        new Thread(backendReactor, "backend-reactor").start();
        requestWorkerPool.start();
        connectionWorkerPool.start();
    }
}