package io.jacksoon.router.pipeline.executor.write;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.context.RouterResponse;
import io.jacksoon.router.pipeline.executor.Depth;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

@Init
public class HttpWrite implements Depth {
    @Override
    public void dodo(PipelineContext context) {
        RouterResponse response = context.getResponse();
        ByteBuffer buffer = context.getByteBuffer();
        response.getBufferContext().setResponseBuffer(buffer);
        SelectionKey key = response.getSelectionKey();
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        key.selector().wakeup();
    }

    @Override
    public String currentEvent() {
        return "backend-response";
    }

    @Override
    public String nextEvent() {
        return "";
    }
}
