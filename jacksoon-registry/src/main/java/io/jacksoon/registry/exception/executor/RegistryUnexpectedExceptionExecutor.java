package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.context.RegistryExceptionContext;

import java.nio.channels.SelectionKey;

@Init
public class RegistryUnexpectedExceptionExecutor implements ExceptionExecutor<Object> {
    private final IOStore ioStore;

    public RegistryUnexpectedExceptionExecutor(IOStore ioStore) {
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
        if (context instanceof RegistryExceptionContext registryContext) {
            RegistryExceptionSupport.respond(ioStore, registryContext, 500, "Internal Server Error");
            return;
        }
        if (context instanceof SelectionKey selectionKey) {
            RegistryExceptionSupport.closeSelectionKey(selectionKey);
        }
    }
}
