package io.jacksoon.router.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.context.RouterExceptionContext;

import java.nio.channels.SelectionKey;

@Init
public class RouterUnexpectedExceptionExecutor implements ExceptionExecutor<Object> {
    private final IOStore ioStore;

    public RouterUnexpectedExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<Object> contextType() {
        return Object.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return Throwable.class;
    }

    @Override
    public void execute(Object context, Throwable throwable) {
        throwable.printStackTrace();

        if (context instanceof RouterExceptionContext routerContext) {
            RouterExceptionSupport.respond(ioStore, routerContext, 500, "Internal Server Error");
            return;
        }
        if (context instanceof SelectionKey selectionKey) {
            RouterExceptionSupport.closeSelectionKey(selectionKey);
        }
    }
}
