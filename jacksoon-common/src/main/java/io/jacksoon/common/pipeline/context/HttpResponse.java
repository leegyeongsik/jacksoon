package io.jacksoon.common.pipeline.context;

import io.jacksoon.common.util.BufferContext;
import lombok.Getter;

import java.nio.channels.SelectionKey;

@Getter
public class HttpResponse {
    private final BufferContext bufferContext;
    private final SelectionKey selectionKey;

    public HttpResponse(BufferContext bufferContext, SelectionKey selectionKey) {
        this.bufferContext = bufferContext;
        this.selectionKey = selectionKey;
    }
}
