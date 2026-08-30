package io.jacksoon.router.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.RouterFilterExecutionException;
import io.jacksoon.router.exception.context.RouterExceptionContext;

@Init
public class RouterFilterExecutionExceptionExecutor implements ExceptionExecutor<RouterExceptionContext> {
    private final IOStore ioStore;

    public RouterFilterExecutionExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<RouterExceptionContext> contextType() {
        return RouterExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RouterFilterExecutionException.class;
    }

    @Override
    public void execute(RouterExceptionContext context, Throwable throwable) {
        throwable.printStackTrace();
        RouterExceptionSupport.respond(ioStore, context, 500, "Internal Server Error");
    }
}
