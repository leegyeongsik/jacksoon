package io.jacksoon.router.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.RouteNotFoundException;
import io.jacksoon.router.exception.context.RouterExceptionContext;

@Init
public class RouteNotFoundExceptionExecutor implements ExceptionExecutor<RouterExceptionContext> {
    private final IOStore ioStore;

    public RouteNotFoundExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<RouterExceptionContext> contextType() {
        return RouterExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RouteNotFoundException.class;
    }

    @Override
    public void execute(RouterExceptionContext context, Throwable throwable) {
        throwable.printStackTrace();
        RouterExceptionSupport.respond(ioStore, context, 404, "Not Found");
    }
}
