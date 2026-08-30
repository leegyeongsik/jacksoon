package io.jacksoon.router.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.RouterFilterException;

@Init
public class RouterFilterExceptionExecutor implements ExceptionExecutor<Void> {
    @Override
    public Class<Void> contextType() {
        return Void.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return RouterFilterException.class;
    }

    @Override
    public void execute(Void context, Throwable throwable) {
        throwable.printStackTrace();
    }
}
