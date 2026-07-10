package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.nio.ByteBuffer;

@Init
public class HttpRouter implements RouterDepth {
    private final FindRouter findRouter;
    private final HttpRequestRewriter requestRewriter;

    public HttpRouter(FindRouter findRouter, HttpRequestRewriter requestRewriter) {
        this.findRouter = findRouter;
        this.requestRewriter = requestRewriter;
    }

    @Override
    public void dodo(RouterPipelineContext context) {
        RoutingTarget target = findRouter.find(context.getRequest());

        ByteBuffer backendRequestBuffer = requestRewriter.rewritePath(
                context.getByteBuffer(),
                context.getRequest().getMethod(),
                target.getBackendPath(),
                context.getRequest().getVersion()
        );

        target.getPool().send(new ProxyContext(backendRequestBuffer, context.getBufferContext(), context.getSelectionKey()));
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