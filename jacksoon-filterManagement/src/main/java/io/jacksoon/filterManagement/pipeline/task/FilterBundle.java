package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Jar;
import io.jacksoon.filterManagement.produce.FilterAction;
import io.jacksoon.filterManagement.produce.FilterProduceDto;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Init
public class FilterBundle implements FilterDepth {
    private final FilterStore filterStore;
    private final Jar jar;
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;
    public FilterBundle(FilterStore filterStore, Jar jar, CommonBlockingQueue<ProduceDto> produceDtoQueue) {
        this.filterStore = filterStore;
        this.jar = jar;
        this.produceDtoQueue = produceDtoQueue;
    }
    // 버전 jar 통합
    @Override
    public void dodo(FilterPipelineContext context) {
        long nextVersion = context.getOperationVersion();
        Path bundlePath = jar.createBundle(context.getCandidateFilters(), nextVersion);
        filterStore.commit(context.getCandidateFilters(), nextVersion, bundlePath);
        produceDtoQueue.put(new FilterProduceDto(context.getCandidateFilters().containsKey(context.getFilterName()) ?
                FilterAction.FILTER_ACTIVE:
                FilterAction.FILTER_DELETE,
                context.getFilterName()));

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
