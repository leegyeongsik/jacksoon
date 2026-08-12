package io.jacksoon.registry.handle;

import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

@Init
public class RegistryRequestSubmitter implements RequestSubmitter {
    private final CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue;

    public RegistryRequestSubmitter(CommonBlockingQueue<RegistryPipelineContext> routerPipelineQueue) {
        this.registryPipelineQueue = routerPipelineQueue;
    }

    @Override
    public void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, SelectionKey selectionKey, AtomicInteger current) {
        RegistryPipelineContext context = new RegistryPipelineContext(
                socketChannel,
                "parse",
                requestBuffer,
                headerLength,
                selectionKey,
                current
        );
        registryPipelineQueue.put(context);
    }
}
