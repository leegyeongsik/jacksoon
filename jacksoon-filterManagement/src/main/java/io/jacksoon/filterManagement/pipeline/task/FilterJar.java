package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterJar implements FilterDepth {
    private final FilterStore filterStore;

    public FilterJar(FilterStore filterStore) {
        this.filterStore = filterStore;
    }

    @Override
    public void dodo(FilterPipelineContext context) {
        // jar로 만들고 디렉토리에 저장
    }

    @Override
    public String currentEvent() {
        return "";
    }
}
