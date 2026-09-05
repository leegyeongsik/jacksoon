package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.common.filter.FilterUploadRequest;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Compile;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterCompile implements FilterDepth {
    private final Compile compile;
    public FilterCompile(Compile compile) {
        this.compile = compile;
    }
    @Override
    public void dodo(FilterPipelineContext context) {
        FilterUploadRequest request = context.getFilterUploadRequest();
        compile.compile(request.fileBytes(), request.config(), context.getOperationVersion());
        context.setEvent("jar");
    }


    @Override
    public String currentEvent() {
        return "compile";
    }
}
