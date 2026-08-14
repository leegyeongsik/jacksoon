package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.pipeline.context.ProxyContext;
import lombok.Getter;

@Getter
public class ReRoutingContext {
    ProxyContext proxyContext;
    String serviceName;
    public ReRoutingContext(ProxyContext proxyContext, String serviceName){
        this.proxyContext = proxyContext;
        this.serviceName = serviceName;
    }
}
