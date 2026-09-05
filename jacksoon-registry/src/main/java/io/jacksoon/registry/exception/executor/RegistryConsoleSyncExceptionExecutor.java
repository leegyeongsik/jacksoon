package io.jacksoon.registry.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.RegistryConsoleSyncException;

@Init
public class RegistryConsoleSyncExceptionExecutor implements ExceptionExecutor<Void> {
    @Override
    public Class<Void> contextType() {
        return Void.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RegistryConsoleSyncException.class;
    }

    @Override
    public void execute(Void context, Throwable throwable) {
        throwable.printStackTrace();
    }
}
