package io.jacksoon.router.pipeline.executor.write;

import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.util.ResponseContext;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

@Init
public class HttpWrite implements RouterDepth {
    private final IOStore ioStore;
    public HttpWrite(IOStore ioStore) {
        this.ioStore = ioStore;
    }
    @Override
    public void dodo(RouterPipelineContext context) {
        ByteBuffer buffer = context.getByteBuffer();
        SelectionKey key = context.getSelectionKey();
        AtomicInteger current = context.getCurrent();
        if (key == null || current == null) {
            return;
        }
        ResponseContext responseContext = new ResponseContext(current.get(), buffer, context.isCloseAfterWrite());
        ioStore.offer(key, responseContext);
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
