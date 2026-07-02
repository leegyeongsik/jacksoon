package io.jacksoon.router.pipeline.context;

import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.common.util.BufferContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class RouterPipelineContext extends PipelineContext {
    public RouterPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext, SelectionKey selectionKey) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, bufferContext, selectionKey);
    }
}
