package io.jacksoon.router.config;

import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.pipeline.executor.router.ReRoutingContext;
import io.jacksoon.router.produce.dto.ServiceRequest;

@Init
public class QueueConfig {
    @Init
    public CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<ProduceDto> produceDtoQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init("serviceMetricQueue")
    public CommonBlockingQueue<ServiceRequest> serviceRequestQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init("filterMetricQueue")
    public CommonBlockingQueue<ServiceRequest> filterRequestQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<ReRoutingContext> reRoutingQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init CommonBlockingQueue<Handler> eventQueue(){
        return new CommonBlockingQueue<>();
    }
}
