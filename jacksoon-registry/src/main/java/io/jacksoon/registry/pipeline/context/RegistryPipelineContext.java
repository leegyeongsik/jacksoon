package io.jacksoon.registry.pipeline.context;

import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
@Getter
@Setter
public class RegistryPipelineContext extends PipelineContext {
    private RegistryRegisterRequest registerRequest;

    public RegistryPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, BufferContext bufferContext, SelectionKey selectionKey) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, bufferContext, selectionKey);
    }
}
