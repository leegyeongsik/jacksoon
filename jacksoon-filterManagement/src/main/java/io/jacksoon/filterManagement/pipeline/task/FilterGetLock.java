package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.util.HashMap;

@Init
public class FilterGetLock implements FilterDepth {
    private final FilterStore filterStore;
    public FilterGetLock(FilterStore filterStore) {
        this.filterStore = filterStore;
    }
    @Override
    public void dodo(FilterPipelineContext context) {
        filterStore.beginUpdate();
        context.setUpdateLockHeld(true);
        context.setOperationVersion(filterStore.version() + 1);
        context.setCandidateFilters(new HashMap<>(filterStore.snapshot()));

        if ("DELETE".equals(context.getRequest().getMethod())) {
            context.setEvent("delete");
        } else {
            context.setEvent("file-check");
        }
    }
    @Override
    public String currentEvent() {
        return "get-lock";
    }
}
