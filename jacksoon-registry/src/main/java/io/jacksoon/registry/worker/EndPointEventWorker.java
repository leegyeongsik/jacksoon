package io.jacksoon.registry.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.connection.event.EndPointEventRegistry;

public class EndPointEventWorker implements Runnable {
    private final CommonBlockingQueue<EndPointEvent> endPointEventQueue;
    private final EndPointEventRegistry registry;

    public EndPointEventWorker(CommonBlockingQueue<EndPointEvent> endPointEventQueue, EndPointEventRegistry registry) {
        this.endPointEventQueue = endPointEventQueue;
        this.registry = registry;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EndPointEvent endPointEvent = endPointEventQueue.take();
                registry.execute(endPointEvent);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
