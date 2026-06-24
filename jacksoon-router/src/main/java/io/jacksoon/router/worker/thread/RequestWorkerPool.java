package io.jacksoon.router.worker.thread;


import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.worker.RequestPipelineWorker;
@Init
public class RequestWorkerPool {
    private final RequestPipelineQueue requestPipelineQueue;
    private final RequestPipelineExecutor requestPipelineExecutor;
    public RequestWorkerPool(RequestPipelineQueue requestPipelineQueue, RequestPipelineExecutor requestPipelineExecutor){
        this.requestPipelineQueue = requestPipelineQueue;
        this.requestPipelineExecutor = requestPipelineExecutor;
    }
    public void start(){
        for (int i = 0; i < 10; i++) {
            new Thread(new RequestPipelineWorker(requestPipelineQueue, requestPipelineExecutor)).start();
        }
    }
}
