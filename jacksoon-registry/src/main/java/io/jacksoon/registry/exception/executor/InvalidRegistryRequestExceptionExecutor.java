package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.InvalidRegistryRequestException;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;

@Init
public class InvalidRegistryRequestExceptionExecutor implements ExceptionExecutor<RegistryExceptionContext> {
    private final IOStore ioStore;

    public InvalidRegistryRequestExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<RegistryExceptionContext> contextType() {
        return RegistryExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return InvalidRegistryRequestException.class;
    }

    @Override
    public void execute(RegistryExceptionContext context, Throwable throwable) {
        throwable.printStackTrace();
        RegistryExceptionSupport.respond(ioStore, context, 400, "Bad Request");
    }
}
