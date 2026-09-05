package io.jacksoon.filterManagement.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.filterManagement.exception.FilterBundleException;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterBundleExceptionExecutor implements ExceptionExecutor<FilterPipelineContext> {
    private final IOStore ioStore;

    public FilterBundleExceptionExecutor(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public Class<FilterPipelineContext> contextType() {
        return FilterPipelineContext.class;
    }

    @Override
    public Class<? extends Throwable> exceptionType() {
        return FilterBundleException.class;
    }

    @Override
    public void execute(FilterPipelineContext context, Throwable throwable) {
        throwable.printStackTrace();
        FilterExceptionSupport.respond(ioStore, context, 500, "Internal Server Error");
    }
}
