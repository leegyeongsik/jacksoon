package io.jacksoon.console.worker.pool;

import io.jacksoon.console.event.EventRegistry;
import io.jacksoon.console.worker.PullWorker;
import io.jacksoon.console.worker.queue.ProduceQueue;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class PullWorkerPool {
    private final ExecutorService pullWorker;
    private final ProduceQueue produceQueue;
    private final EventRegistry eventRegistry;
    public PullWorkerPool(ExecutorService pullWorker, ProduceQueue produceQueue, EventRegistry eventRegistry) {
        this.pullWorker = pullWorker;
        this.produceQueue = produceQueue;
        this.eventRegistry = eventRegistry;
    }

    @PostConstruct
    public void start() {
        for (int i = 0; i < 2; i++) {
            pullWorker.submit(new PullWorker(produceQueue,eventRegistry));
        }
    }
}
