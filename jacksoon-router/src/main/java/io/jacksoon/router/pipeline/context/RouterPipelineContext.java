package io.jacksoon.router.pipeline.context;

import io.jacksoon.common.pipeline.context.PipelineContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class RouterPipelineContext extends PipelineContext {
    public RouterPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, SelectionKey selectionKey,AtomicInteger current) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, selectionKey,current);
    }
}
