package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.worker.connection.RequestBackendQueue;
import lombok.Getter;

import java.nio.channels.SelectionKey;
@Getter
public class ConnectionContexts {
    private final SelectionKey selectionKey;
    private final RequestBackendQueue requestBackendQueue;

    public ConnectionContexts(SelectionKey selectionKey, RequestBackendQueue requestBackendQueue) {
        this.selectionKey = selectionKey;
        this.requestBackendQueue = requestBackendQueue;
    }
}
