package io.jacksoon.common.worker;


public interface Executor<T> {
    void execute(T pipelineContext);
}
