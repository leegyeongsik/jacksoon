package io.jacksoon.router.pipeline.context;


import io.jacksoon.common.util.BufferContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class ProxyContext {
    public final SelectionKey clientKey;
    public final ByteBuffer requestBuffer;
    public final ByteBuffer readBuffer;
    public final ByteBuffer responseBuffer;
    public final BufferContext bufferContext;

    public ProxyContext(ByteBuffer requestBuffer, BufferContext bufferContext, SelectionKey clientKey) {
        this.requestBuffer = requestBuffer;
        this.readBuffer = ByteBuffer.allocate(8 * 1024);
        this.responseBuffer = ByteBuffer.allocate(10 * 1024 * 1024);
        this.bufferContext = bufferContext;
        this.clientKey = clientKey;
    }
}