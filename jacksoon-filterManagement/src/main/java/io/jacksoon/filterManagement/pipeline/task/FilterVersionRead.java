package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterVersionRead implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        // 버전을 줌 -> write

    }

    @Override
    public String currentEvent() {
        return "";
    }
}
