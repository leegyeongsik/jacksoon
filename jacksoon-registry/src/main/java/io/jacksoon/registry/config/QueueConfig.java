package io.jacksoon.registry.config;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.connection.EndpointConnectionContext;
import io.jacksoon.registry.connection.event.EndPointEvent;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

@Init
public class QueueConfig {

    @Init
    public CommonBlockingQueue<RegistryPipelineContext> registryPipelineQueue() {
        return new CommonBlockingQueue<>();
    }

    @Init
    public CommonBlockingQueue<EndpointConnectionContext> endpointConnectionQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<EndPointEvent> endpointEventQueue() {
        return new CommonBlockingQueue<>();
    }

    @Init
    public CommonBlockingQueue<ProduceDto> produceDtoQueue() {
        return new CommonBlockingQueue<>();
    }
}