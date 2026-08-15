package io.jacksoon.common.worker;

import io.jacksoon.common.selector.EventManagement;
import io.jacksoon.common.selector.EventWarrap;
import io.jacksoon.common.util.CommonBlockingQueue;

import java.util.Objects;

public class EventWorker implements Runnable{
    private final CommonBlockingQueue<EventWarrap> eventWarrapQueue;
    private final EventManagement eventManagement;
    public EventWorker(CommonBlockingQueue<EventWarrap> eventWarrapQueue, EventManagement eventManagement) {
        this.eventWarrapQueue = eventWarrapQueue;
        this.eventManagement = eventManagement;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EventWarrap event = eventWarrapQueue.take();
                if (!Objects.equals(eventManagement.checkPeekPendingEvent(event), event.getN())) {
                    continue;
                }
                try {
                    event.getHandler().handle();
                } finally {
                    Long next = eventManagement.completeEvent(event.getHandler());
                    if (next != null) {
                        eventWarrapQueue.put(new EventWarrap(event.getHandler(), next));
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
