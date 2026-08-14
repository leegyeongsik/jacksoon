package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.init.annotation.Init;

@Init
public class HttpReRouter {
    private final FindRouter findRouter;

    public HttpReRouter(FindRouter findRouter) {
        this.findRouter = findRouter;
    }
    public void dodo(ReRoutingContext reRoutingContext){
        findRouter.getServiceGroup(reRoutingContext.serviceName).send(reRoutingContext.getProxyContext());
    }
}
