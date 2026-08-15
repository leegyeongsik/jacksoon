package io.jacksoon.common.worker;

import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.selector.EventManagement;
import io.jacksoon.common.util.CommonBlockingQueue;

public class EventWorker implements Runnable {

    private final CommonBlockingQueue<Handler> eventQueue;
    private final EventManagement eventManagement;

    public EventWorker(CommonBlockingQueue<Handler> eventQueue, EventManagement eventManagement) {
        this.eventQueue = eventQueue;
        this.eventManagement = eventManagement;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Handler handler = eventQueue.take();
                try {
                    handler.handle();
                } finally {
                    boolean rerun = eventManagement.completeEvent(handler);
                    if (rerun) {
                        eventQueue.put(handler);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}