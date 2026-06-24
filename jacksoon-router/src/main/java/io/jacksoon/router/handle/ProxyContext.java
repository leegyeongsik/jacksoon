package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ProxyContext {
    final SelectionKey clientKey;
    public final ByteBuffer requestBuffer;
    public final ByteBuffer readBuffer;
    public final ByteBuffer responseBuffer;
    final BufferContext bufferContext;

    public ProxyContext(ByteBuffer requestBuffer, BufferContext bufferContext, SelectionKey clientKey) {
        this.requestBuffer = requestBuffer;
        this.readBuffer = ByteBuffer.allocate(8 * 1024);
        this.responseBuffer = ByteBuffer.allocate(10 * 1024 * 1024);
        this.bufferContext = bufferContext;
        this.clientKey = clientKey;
    }
}