package io.jacksoon.router.pipeline.executor.write;

import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

@Init
public class HttpWrite implements RouterDepth {
    @Override
    public void dodo(RouterPipelineContext context) {
        ByteBuffer buffer = context.getByteBuffer();
        context.getBufferContext().setResponseBuffer(buffer);
        SelectionKey key = context.getSelectionKey();
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }

    @Override
    public String currentEvent() {
        return "backend-response";
    }

    @Override
    public String nextEvent() {
        return null;
    }
}
