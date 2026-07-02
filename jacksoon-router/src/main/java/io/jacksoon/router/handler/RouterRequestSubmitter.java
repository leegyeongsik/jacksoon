package io.jacksoon.router.handler;

import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Init
public class RouterRequestSubmitter implements RequestSubmitter {
    private final CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue;

    public RouterRequestSubmitter(CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue) {
        this.routerPipelineQueue = routerPipelineQueue;
    }

    @Override
    public void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, BufferContext bufferContext, SelectionKey selectionKey) {
        RouterPipelineContext context = new RouterPipelineContext(
                socketChannel,
                "parse",
                requestBuffer,
                headerLength,
                bufferContext,
                selectionKey
        );
        routerPipelineQueue.put(context);
    }
}
