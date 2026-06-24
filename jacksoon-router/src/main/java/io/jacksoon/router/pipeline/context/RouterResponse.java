package io.jacksoon.router.pipeline.context;

import io.jacksoon.router.help.BufferContext;
import lombok.Getter;

import java.nio.channels.SelectionKey;

@Getter
public class RouterResponse {
    private final BufferContext bufferContext;
    private final SelectionKey selectionKey;

    public RouterResponse(BufferContext bufferContext, SelectionKey selectionKey) {
        this.bufferContext = bufferContext;
        this.selectionKey = selectionKey;
    }
}
