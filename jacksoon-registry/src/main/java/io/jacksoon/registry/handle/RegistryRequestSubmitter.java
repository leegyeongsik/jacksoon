package io.jacksoon.registry.handle;

import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Init
public class RegistryRequestSubmitter implements RequestSubmitter {
    private final CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue;
    public RegistryRequestSubmitter(CommonBlockingQueue<RegistryPipelineContext> routerPipelineQueue) {
        this.registryPipelineQueue = routerPipelineQueue;
    }
    @Override
    public void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, BufferContext bufferContext, SelectionKey selectionKey) {
        RegistryPipelineContext context = new RegistryPipelineContext(
                socketChannel,
                "parse",
                requestBuffer,
                headerLength,
                bufferContext,
                selectionKey
        );
        registryPipelineQueue.put(context);
    }
}
