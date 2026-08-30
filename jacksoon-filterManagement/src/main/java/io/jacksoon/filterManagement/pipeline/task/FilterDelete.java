package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.exception.InvalidFilterRequestException;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterDelete implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        String filterName = context.getFilterName();
        if (context.getCandidateFilters().remove(filterName) == null) {
            throw new InvalidFilterRequestException("Filter not found. filterName=" + filterName);
        }
        context.setEvent("bundle");
    }
    @Override
    public String currentEvent() {
        return "delete";
    }
}
