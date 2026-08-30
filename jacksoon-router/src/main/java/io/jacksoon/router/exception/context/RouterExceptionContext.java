package io.jacksoon.router.exception.context;

import io.jacksoon.router.pipeline.context.ProxyContext;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import lombok.Getter;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public final class RouterExceptionContext {
    private final RouterPipelineContext pipelineContext;
    private final ReRoutingContext reRoutingContext;
    private final ProxyContext proxyContext;

    private RouterExceptionContext(RouterPipelineContext pipelineContext, ReRoutingContext reRoutingContext, ProxyContext proxyContext) {
        this.pipelineContext = pipelineContext;
        this.reRoutingContext = reRoutingContext;
        this.proxyContext = proxyContext;
    }

    public static RouterExceptionContext of(RouterPipelineContext context) {
        return new RouterExceptionContext(context, null, null);
    }

    public static RouterExceptionContext of(ReRoutingContext context) {
        return new RouterExceptionContext(null, context, null);
    }

    public static RouterExceptionContext of(ProxyContext context) {
        return new RouterExceptionContext(null, null, context);
    }

    public ProxyContext getProxyContext() {
        if (proxyContext != null) {
            return proxyContext;
        }
        if (reRoutingContext != null) {
            return reRoutingContext.getProxyContext();
        }
        return null;
    }

    public SelectionKey getClientSelectionKey() {
        if (pipelineContext != null) {
            return pipelineContext.getSelectionKey();
        }
        ProxyContext proxy = getProxyContext();
        return proxy == null ? null : proxy.clientKey;
    }

    public AtomicInteger getCurrent() {
        if (pipelineContext != null) {
            return pipelineContext.getCurrent();
        }
        ProxyContext proxy = getProxyContext();
        return proxy == null ? null : proxy.current;
    }
}
