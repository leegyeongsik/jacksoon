package io.jacksoon.registry.worker;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.store.RegistryStore;

public class EndPointEventWorker implements Runnable{
    private final CommonBlockingQueue<EndPointEvent> endPointEventQueue;
    private final RegistryStore registryStore;
    public EndPointEventWorker(CommonBlockingQueue<EndPointEvent> endPointEventQueue, RegistryStore registryStore) {
        this.endPointEventQueue = endPointEventQueue;
        this.registryStore = registryStore;
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EndPointEvent endPointEvent = endPointEventQueue.take();
                registryStore.removeEndpoint(endPointEvent.getServiceName(),endPointEvent.getInstanceId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
