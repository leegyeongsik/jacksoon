package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterDelete implements FilterDepth {
    private final FilterStore filterStore;

    public FilterDelete(FilterStore filterStore) {
        this.filterStore = filterStore;
    }
    @Override
    public void dodo(FilterPipelineContext context) {
        // 여기서 세팅갱신하고 번들로 넘기셈

    }

    @Override
    public String currentEvent() {
        return "";
    }
}
