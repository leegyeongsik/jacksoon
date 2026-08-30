package io.jacksoon.registry.exception.context;

import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import lombok.Getter;

import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public final class RegistryExceptionContext {
    private final RegistryPipelineContext pipelineContext;
    private final EndpointConnectionContext endpointConnectionContext;

    private RegistryExceptionContext(RegistryPipelineContext pipelineContext, EndpointConnectionContext endpointConnectionContext) {
        this.pipelineContext = pipelineContext;
        this.endpointConnectionContext = endpointConnectionContext;
    }

    public static RegistryExceptionContext of(RegistryPipelineContext context) {
        return new RegistryExceptionContext(context, null);
    }

    public static RegistryExceptionContext of(EndpointConnectionContext context) {
        return new RegistryExceptionContext(null, context);
    }

    public SelectionKey getRequestSelectionKey() {
        return pipelineContext == null ? null : pipelineContext.getSelectionKey();
    }

    public AtomicInteger getCurrent() {
        return pipelineContext == null ? null : pipelineContext.getCurrent();
    }
}
