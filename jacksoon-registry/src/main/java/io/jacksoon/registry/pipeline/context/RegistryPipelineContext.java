package io.jacksoon.registry.pipeline.context;

import io.jacksoon.common.pipeline.context.PipelineContext;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
@Getter
@Setter
public class RegistryPipelineContext extends PipelineContext {
    private RegistryRegisterRequest registerRequest;

    public RegistryPipelineContext(SocketChannel socketChannel, String event, ByteBuffer byteBuffer, int byteBufferIndex, SelectionKey selectionKey, AtomicInteger current) {
        super(socketChannel, event, byteBuffer, byteBufferIndex, selectionKey, current);
    }
}
