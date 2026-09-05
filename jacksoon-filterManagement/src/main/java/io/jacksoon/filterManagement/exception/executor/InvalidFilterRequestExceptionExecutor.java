package io.jacksoon.filterManagement.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.filterManagement.exception.InvalidFilterRequestException;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

@Init
public class InvalidFilterRequestExceptionExecutor implements ExceptionExecutor<FilterPipelineContext> {
    private final IOStore ioStore;

    public InvalidFilterRequestExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<FilterPipelineContext> contextType() {
        return FilterPipelineContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return InvalidFilterRequestException.class;
    }

    @Override
    public void execute(FilterPipelineContext context, Throwable throwable) {
        throwable.printStackTrace();
        FilterExceptionSupport.respond(ioStore, context, 400, "Bad Request");
    }
}
