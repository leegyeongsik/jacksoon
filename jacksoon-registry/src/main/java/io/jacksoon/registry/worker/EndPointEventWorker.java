package io.jacksoon.registry.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.connection.event.EndPointEventRegistry;
import io.jacksoon.registry.exception.RegistryEventException;

public class EndPointEventWorker implements Runnable {
    private final CommonBlockingQueue<EndPointEvent> endPointEventQueue;
    private final EndPointEventRegistry registry;
    private final ExceptionDispatcher exceptionDispatcher;

    public EndPointEventWorker(CommonBlockingQueue<EndPointEvent> endPointEventQueue, EndPointEventRegistry registry, ExceptionDispatcher exceptionDispatcher) {
        this.endPointEventQueue = endPointEventQueue;
        this.registry = registry;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            EndPointEvent event;
            try {
                event = endPointEventQueue.take();
                registry.execute(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                RegistryEventException exception = e instanceof RegistryEventException registryEventException
                        ? registryEventException
                        : new RegistryEventException("Failed to execute endpoint event", e);
                exceptionDispatcher.dispatch(exception);
            }
        }
    }
}
