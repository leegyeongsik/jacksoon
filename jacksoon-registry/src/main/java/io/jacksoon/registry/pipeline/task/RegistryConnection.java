package io.jacksoon.registry.pipeline.task;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;

@Init
public class RegistryConnection implements RegistryDepth {
    private final CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue;

    public RegistryConnection(CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue) {
        this.endpointConnectionQueue = endpointConnectionQueue;
    }

    @Override
    public void dodo(RegistryPipelineContext context) {
        RegistryRegisterRequest request = context.getRegisterRequest();

        if (request == null) {
            throw new IllegalStateException("registerRequest is null");
        }
        EndpointConnectionContext connectionContext = new EndpointConnectionContext(request.getServiceName(), request.getInstanceId(), request.getEndpoint().getHost(), request.getEndpoint().getPort(), request.getEndpoint().getProtocol(), request.getEndpoint().getHealthPath());
        endpointConnectionQueue.put(connectionContext);
        context.setEvent("write");
    }
    @Override
    public String currentEvent() {
        return "connection";
    }
}