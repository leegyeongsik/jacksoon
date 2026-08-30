package io.jacksoon.router.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.BackendConnectionException;
import io.jacksoon.router.exception.context.RouterExceptionContext;

@Init
public class BackendConnectionExceptionExecutor implements ExceptionExecutor<RouterExceptionContext> {
    private final IOStore ioStore;

    public BackendConnectionExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<RouterExceptionContext> contextType() {
        return RouterExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return BackendConnectionException.class;
    }

    @Override
    public void execute(RouterExceptionContext context, Throwable throwable) {
        throwable.printStackTrace();
        RouterExceptionSupport.respond(ioStore, context, 502, "Bad Gateway");
    }
}
