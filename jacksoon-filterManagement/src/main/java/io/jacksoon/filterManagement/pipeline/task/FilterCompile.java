package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterCompile implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        // 여기서 class로 만드는데 근데 버전 갱신을 언제하지?
    }

    @Override
    public String currentEvent() {
        return "";
    }
}
