package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.common.connection.ConnectionContexts;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.nio.channels.SelectionKey;

@Init
public class HttpRouter implements RouterDepth {
    private final FindRouter findRouter;
    public HttpRouter(FindRouter findRouter) {
        this.findRouter = findRouter;
    }

    @Override
    public void dodo(RouterPipelineContext context) {
        ConnectionContexts<ProxyContext> connectionContext  = findRouter.getConnection(context.getRequest());
        connectionContext.requestBackendQueue().put(new ProxyContext(context.getByteBuffer(),context.getResponse().getBufferContext(),context.getResponse().getSelectionKey()));
        SelectionKey key = connectionContext.selectionKey();
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        connectionContext.selectionKey().selector().wakeup();
    }
    @Override
    public String currentEvent() {
        return "router";
    }
    @Override
    public String nextEvent() {
        return null;
    }
}
