package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;

@Init
public class FilterVersionRead implements FilterDepth {

    private final FilterStore filterStore;

    public FilterVersionRead(FilterStore filterStore) {
        this.filterStore = filterStore;
    }

    @Override
    public void dodo(FilterPipelineContext context) {
        ByteBuffer body = ByteBuffer.allocate(Long.BYTES);
        body.putLong(filterStore.version());
        body.flip();
        context.getResponse().setStatusCode(200);
        context.getResponse().setReasonPhrase("OK");
        context.getResponse().setWriteBuffer(body);
        context.getResponse().addHeader("Content-Type", "application/octet-stream");
        context.setEvent("write");
    }

    @Override
    public String currentEvent() {
        return "version-read";
    }
}