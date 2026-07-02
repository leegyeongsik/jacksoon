package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handler.BackendIOHandler;
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
        BackendIOHandler handler = findRouter.getConnection(context.getRequest());
        handler.send(new ProxyContext(context.getByteBuffer(), context.getBufferContext(), context.getSelectionKey()));
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
