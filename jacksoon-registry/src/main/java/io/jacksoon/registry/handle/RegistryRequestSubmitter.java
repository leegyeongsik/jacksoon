package io.jacksoon.registry.handle;

import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Init
public class RegistryRequestSubmitter implements RequestSubmitter {
    // queue에 넣어놓고 일단 parse에
    public RegistryRequestSubmitter() {
    }

    @Override
    public void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, BufferContext bufferContext, SelectionKey selectionKey) {
    }
}
