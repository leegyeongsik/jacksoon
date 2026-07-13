package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Jar;
import io.jacksoon.filterManagement.store.FilterDefinition;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

@Init
public class FilterBundle implements FilterDepth {
    private  final FilterStore filterStore;
    private final Jar jar;
    public FilterBundle(FilterStore filterStore, Jar jar) {
        this.filterStore = filterStore;
        this.jar = jar;
    }
    // 버전 jar 통합
    @Override
    public void dodo(FilterPipelineContext context) {
        long nextVersion = context.getOperationVersion();
        Path bundlePath = jar.createBundle(context.getCandidateFilters(), nextVersion);
        filterStore.commit(context.getCandidateFilters(), nextVersion, bundlePath);
        byte[] body = ("filter bundle updated: version=" + nextVersion).getBytes(StandardCharsets.UTF_8);
        context.getResponse().setWriteBuffer(ByteBuffer.wrap(body));
        context.getResponse().addHeader("Content-Type", "text/plain; charset=UTF-8");
        context.setEvent("release");
    }


    @Override
    public String currentEvent() {
        return "bundle";
    }
}
