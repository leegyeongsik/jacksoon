package io.jacksoon.router.worker.thread;


import io.jacksoon.router.worker.RequestPipelineWorker;

public class RequestWorkerPool {
    private final RequestPipelineQueue requestPipelineQueue;
    RequestWorkerPool(RequestPipelineQueue requestPipelineQueue){
        this.requestPipelineQueue = requestPipelineQueue;
    }
    void start(){
        for (int i = 0; i < 10; i++) {
            new Thread(new RequestPipelineWorker(requestPipelineQueue, new RequestPipelineExecutor())).start();
        }
    }
}
