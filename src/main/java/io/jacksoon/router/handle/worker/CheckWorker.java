package io.jacksoon.router.handle.worker;


public class CheckWorker implements Runnable {
    Executor executor;
    CheckQueue checkQueue;
    public CheckWorker(CheckQueue checkQueue, Executor executor) {
        this.checkQueue = checkQueue;
        this.executor = executor;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                CheckContext checkContext = checkQueue.take();
                executor.executor(checkContext);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
