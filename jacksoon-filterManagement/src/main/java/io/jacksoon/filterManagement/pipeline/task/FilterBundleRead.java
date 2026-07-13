package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Jar;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

@Init
public class FilterBundleRead implements FilterDepth {
    private final FilterStore filterStore;

    public FilterBundleRead(FilterStore filterStore) {
        this.filterStore = filterStore;
    }
    // 여기선 번들 jar파일을 byte에 담아서 -> write

    @Override
    public void dodo(FilterPipelineContext context) {
        Path bundlePath = filterStore.currentBundle();
        if (bundlePath == null || !Files.isRegularFile(bundlePath)) {
            context.getResponse().setStatusCode(404);
            context.getResponse().setReasonPhrase("Not Found");
            context.getResponse().setWriteBuffer(ByteBuffer.wrap(new byte[0]));
            context.setEvent("write");
            return;
        }

        try {
            context.getResponse().setWriteBuffer(ByteBuffer.wrap(Files.readAllBytes(bundlePath)));
            context.getResponse().addHeader("Content-Type", "application/java-archive");
            context.setEvent("write");
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public String currentEvent() {
        return "bundle-read";
    }
}