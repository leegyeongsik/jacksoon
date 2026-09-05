package io.jacksoon.registry.config;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.exception.ExceptionExecutorRegistry;
import io.jacksoon.init.annotation.Init;

import java.util.List;

@Init
public class ExceptionConfig {
    @Init
    public ExceptionExecutorRegistry exceptionExecutorRegistry(List<ExceptionExecutor<?>> executors) {
        return new ExceptionExecutorRegistry(executors);
    }

    @Init
    public ExceptionDispatcher exceptionDispatcher(ExceptionExecutorRegistry registry) {
        return new ExceptionDispatcher(registry);
    }
}
