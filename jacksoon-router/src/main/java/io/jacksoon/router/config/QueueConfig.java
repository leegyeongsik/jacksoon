package io.jacksoon.router.config;

import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.common.connection.ConnectionContext;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;

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
}
