package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterBundle implements FilterDepth {
    private  final FilterStore filterStore;

    public FilterBundle(FilterStore filterStore) {
        this.filterStore = filterStore;

    }

    @Override
    public void dodo(FilterPipelineContext context) {
        // 버전 jar 통합
    }

    @Override
    public String currentEvent() {
        return "";
    }
}
