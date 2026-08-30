package io.jacksoon.filterManagement.exception.executor;

import io.jacksoon.common.exception.ExceptionExecutor;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.init.annotation.Init;

import java.nio.channels.SelectionKey;

@Init
public class FilterUnexpectedExceptionExecutor implements ExceptionExecutor<Object> {
    private final IOStore ioStore;

    public FilterUnexpectedExceptionExecutor(IOStore ioStore) {
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
        if (context instanceof FilterPipelineContext pipelineContext) {
            FilterExceptionSupport.respond(ioStore, pipelineContext, 500, "Internal Server Error");
            return;
        }
        if (context instanceof SelectionKey selectionKey) {
            FilterExceptionSupport.closeSelectionKey(selectionKey);
        }
    }
}
