package io.jacksoon.common.exception;

public interface ExceptionExecutor<C> {
    Class<C> contextType();

    Class<? extends Throwable> exceptionType();

    void execute(C context, Throwable throwable);
}
