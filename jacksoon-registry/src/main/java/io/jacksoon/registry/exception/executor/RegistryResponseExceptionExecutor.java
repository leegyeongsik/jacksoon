package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.RegistryResponseException;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;

@Init
public class RegistryResponseExceptionExecutor implements ExceptionExecutor<RegistryExceptionContext> {
    private final IOStore ioStore;

    public RegistryResponseExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<RegistryExceptionContext> contextType() {
        return RegistryExceptionContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RegistryResponseException.class;
    }

    @Override
    public void execute(RegistryExceptionContext context, Throwable throwable) {
        throwable.printStackTrace();
        RegistryExceptionSupport.respond(ioStore, context, 500, "Internal Server Error");
    }
}
