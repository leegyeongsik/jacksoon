package io.jacksoon.router.config;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.connection.ConnectionContext;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.produce.dto.ServiceRequest;

@Init
public class QueueConfig {
    @Init
    public CommonBlockingQueue<ConnectionContext> connectionContextQueue() {
        return new CommonBlockingQueue<>();
    }

    @Init
    public CommonBlockingQueue<RouterPipelineContext> routerPipelineQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<ProduceDto> produceDtoQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<ServiceRequest> serviceRequestQueue() {
        return new CommonBlockingQueue<>();
    }

}
