package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterRead implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        // 여기선 세팅을 주는걸로 하자 -> write
    }

    @Override
    public String currentEvent() {
        return "";
    }
}
