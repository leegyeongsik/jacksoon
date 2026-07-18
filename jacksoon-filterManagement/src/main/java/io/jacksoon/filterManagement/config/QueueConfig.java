package io.jacksoon.filterManagement.config;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;
@Init
public class QueueConfig {
    @Init
    public CommonBlockingQueue<FilterPipelineContext> filterPipelineQueue() {
        return new CommonBlockingQueue<>();
    }
    @Init
    public CommonBlockingQueue<ProduceDto> produceDtoQueue() {
        return new CommonBlockingQueue<>();
    }
}