package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterDelete implements FilterDepth {
    private final FilterStore filterStore;
    // 여기서 세팅갱신하고 번들로 넘기셈

    public FilterDelete(FilterStore filterStore) {
        this.filterStore = filterStore;
    }
    @Override
    public void dodo(FilterPipelineContext context) {
        String filterName = context.getFilterName();
        if (context.getCandidateFilters().remove(filterName) == null) {
            throw new IllegalArgumentException();
        }
        context.setEvent("bundle");
    }

    @Override
    public String currentEvent() {
        return "delete";
    }
}
