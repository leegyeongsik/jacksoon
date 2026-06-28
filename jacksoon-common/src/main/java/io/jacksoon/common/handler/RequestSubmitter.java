package io.jacksoon.common.handler;

import io.jacksoon.common.util.BufferContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface RequestSubmitter {
    void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, BufferContext bufferContext, SelectionKey selectionKey);
}