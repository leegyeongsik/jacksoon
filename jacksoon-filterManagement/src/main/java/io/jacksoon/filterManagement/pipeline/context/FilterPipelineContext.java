package io.jacksoon.filterManagement.pipeline.context;

import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.common.util.BufferContext;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
@Getter
@Setter
public class FilterPipelineContext extends PipelineContext {
    private FilterUploadRequest filterUploadRequest;

    public FilterPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext, SelectionKey selectionKey) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, bufferContext, selectionKey);
    }
}
