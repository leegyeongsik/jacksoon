package io.jacksoon.router.handle.worker;

import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.pipeline.step.Step;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

public class CheckWorkerPool {
    private final CheckQueue checkQueue;
    private final RequestPipelineQueue requestPipelineQueue;
    private final RequestCheck requestCheck;
    private final Step step;
    public CheckWorkerPool(CheckQueue checkQueue, RequestPipelineQueue requestPipelineQueue, RequestCheck requestCheck, Step step){
        this.checkQueue = checkQueue;
        this.requestPipelineQueue = requestPipelineQueue;
        this.requestCheck = requestCheck;
        this.step = step;

    }
    public void start(){
        for (int i = 0; i < 10; i++) {
            new Thread(new CheckWorker(checkQueue, new CheckExecutor(requestCheck,requestPipelineQueue,step))).start();
        }
    }
}
