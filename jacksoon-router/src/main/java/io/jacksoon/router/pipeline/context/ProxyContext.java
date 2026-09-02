package io.jacksoon.router.pipeline.context;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyContext {
    public final SelectionKey clientKey;
    public final ByteBuffer requestBuffer;
    public ByteBuffer responseBuffer;
    public final AtomicInteger current;

    public ProxyContext(ByteBuffer requestBuffer, SelectionKey clientKey, AtomicInteger current) {
        this.requestBuffer = requestBuffer;
        this.current = current;
        this.responseBuffer = ByteBuffer.allocate(8 * 1024);
        this.clientKey = clientKey;
    }
}