package io.jacksoon.common.handler;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public interface RequestSubmitter {
    void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, SelectionKey selectionKey, AtomicInteger current);
}