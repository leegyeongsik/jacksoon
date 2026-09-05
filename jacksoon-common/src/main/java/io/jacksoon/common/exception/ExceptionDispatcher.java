package io.jacksoon.common.exception;

public class ExceptionDispatcher {
    private final ExceptionExecutorRegistry executorRegistry;

    public ExceptionDispatcher(ExceptionExecutorRegistry executorRegistry) {
        this.executorRegistry = executorRegistry;
    }

    public <C> void dispatch(C context, Throwable throwable) {
        ExceptionExecutor<C> executor = executorRegistry.get(context, throwable);
        executor.execute(context, throwable);
        //원래구조에서는 excute실행할때 안에서 타입캐스팅 했어야했는데 제네릭으로 타입선언해서 익스큐터 찾으니까 거기에 맞는 context를 넣을 수 있음
    }

    public void dispatch(Throwable throwable) {
        ExceptionExecutor<Void> executor = executorRegistry.get(throwable);
        executor.execute(null, throwable);
    }
}
