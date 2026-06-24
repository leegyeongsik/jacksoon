package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handle.ProxyContext;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.executor.Depth;

import java.nio.channels.SelectionKey;

@Init
public class HttpRouter implements Depth {
    private final FindRouter findRouter;
    public HttpRouter(FindRouter findRouter) {
        this.findRouter = findRouter;
    }

    @Override
    public void dodo(PipelineContext context) {
        ConnectionContexts connectionContext  = findRouter.getConnection(context.getRequest());
        connectionContext.getRequestBackendQueue().put(new ProxyContext(context.getByteBuffer(),context.getResponse().getBufferContext(),context.getResponse().getSelectionKey()));
        SelectionKey key = connectionContext.getSelectionKey();
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        connectionContext.getSelectionKey().selector().wakeup();
    }
    @Override
    public String currentEvent() {
        return "router";
    }
    @Override
    public String nextEvent() {
        return "";
    }
}
