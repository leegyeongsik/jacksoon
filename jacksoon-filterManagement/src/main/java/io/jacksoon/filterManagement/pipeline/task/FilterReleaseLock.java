package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterReleaseLock implements FilterDepth {
    private final FilterStore filterStore;

    public FilterReleaseLock(FilterStore filterStore) {
        this.filterStore = filterStore;
    }

    @Override
    public void dodo(FilterPipelineContext context) {
        if (context.isUpdateLockHeld()) {
            filterStore.completeUpdate();
            context.setUpdateLockHeld(false);
        }
        context.setEvent("write");
    }

    @Override
    public String currentEvent() {
        return "release";
    }
}
