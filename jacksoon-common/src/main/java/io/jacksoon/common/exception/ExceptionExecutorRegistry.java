package io.jacksoon.common.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionExecutorRegistry {
    private final Map<Class<? extends Throwable>, ExceptionExecutor<?>> executorMap;

    public ExceptionExecutorRegistry(List<ExceptionExecutor<?>> executors) {
        if (executors == null || executors.isEmpty()) {
            throw new IllegalArgumentException("ExceptionExecutor list must not be empty");
        }
        this.executorMap = new HashMap<>();
        for (ExceptionExecutor<?> executor : executors) {
            register(executor);
        }
    }

    private void register(ExceptionExecutor<?> executor) {
        if (executor == null) {
            throw new IllegalArgumentException("ExceptionExecutor must not be null");
        }
        Class<? extends Throwable> exceptionType = executor.exceptionType();
        if (exceptionType == null) {
            throw new IllegalArgumentException("exceptionType is null: " + executor.getClass().getName());
        }
        if (executor.contextType() == null) {
            throw new IllegalArgumentException("contextType is null: " + executor.getClass().getName());
        }
        ExceptionExecutor<?> previous = executorMap.putIfAbsent(exceptionType, executor);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate ExceptionExecutor. exceptionType=" + exceptionType.getName());
        }
    }

    public <C> ExceptionExecutor<C> get(C context, Throwable throwable) {
        if (throwable == null) {
            throw new IllegalArgumentException("throwable must not be null");
        }
        Class<?> current = throwable.getClass();
        while (current != null && Throwable.class.isAssignableFrom(current)) {
            @SuppressWarnings("unchecked")
            Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) current;
            ExceptionExecutor<?> executor = executorMap.get(exceptionType);
            if (executor != null) {
                validateContext(executor, context);
                return cast(executor);
            }
            current = current.getSuperclass();
        }
        throw new IllegalStateException("No ExceptionExecutor found. exception=" + throwable.getClass().getName());
    }

    public ExceptionExecutor<Void> get(Throwable throwable) {
        return get(null, throwable);
    }

    private <C> void validateContext(ExceptionExecutor<?> executor, C context) {
        Class<?> contextType = executor.contextType();
        if (contextType.equals(Object.class)) {
            return;
        }
        if (context == null) {
            if (!contextType.equals(Void.class)) {
                throw new IllegalStateException("ExceptionExecutor requires context. " + "exceptionType=" + executor.exceptionType().getName() + ", contextType=" + contextType.getName());
            }
            return;
        }
        if (!contextType.isInstance(context)) {
            throw new IllegalStateException("Invalid exception context. expected=" + contextType.getName() + ", actual=" + context.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static <C> ExceptionExecutor<C> cast(ExceptionExecutor<?> executor) {
        return (ExceptionExecutor<C>) executor;
    }
}