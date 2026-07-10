package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterBundleRead implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        // 여기선 번들 jar파일을 byte에 담아서 -> write

    }

    @Override
    public String currentEvent() {
        return "";
    }
}
