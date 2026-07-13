package io.jacksoon.filterManagement.handle;

import io.jacksoon.common.handler.RequestSubmitter;
import io.jacksoon.common.util.BufferContext;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

@Init
public class FilterRequestSubmitter implements RequestSubmitter {
    private final CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue;
    public FilterRequestSubmitter(CommonBlockingQueue<FilterPipelineContext> routerPipelineQueue) {
        this.filterPipelineQueue = routerPipelineQueue;
    }
    @Override
    public void submit(SocketChannel socketChannel, ByteBuffer requestBuffer, int headerLength, BufferContext bufferContext, SelectionKey selectionKey) {
        FilterPipelineContext context = new FilterPipelineContext(
                socketChannel,
                "parse",
                requestBuffer,
                headerLength,
                bufferContext,
                selectionKey
        );
        filterPipelineQueue.put(context);
    }
}
