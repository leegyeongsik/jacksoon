package io.jacksoon.console.worker;

import io.jacksoon.console.dto.request.BaseProduceDto;
import io.jacksoon.console.event.EventRegistry;
import io.jacksoon.console.worker.queue.ProduceQueue;

public class PullWorker implements Runnable{
    private final ProduceQueue produceQueue;
    private final EventRegistry registry;
    public PullWorker(ProduceQueue produceQueue, EventRegistry registry) {
        this.produceQueue = produceQueue;
        this.registry = registry;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try {
                BaseProduceDto baseProduceDto =  produceQueue.take();
                registry.execute(baseProduceDto);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
