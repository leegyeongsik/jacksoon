package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.common.filter.FilterUploadRequest;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Jar;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterJarUpload implements FilterDepth {
    private final Jar jar;

    public FilterJarUpload(Jar jar) {
        this.jar = jar;
    }

    @Override
    public void dodo(FilterPipelineContext context) {
        FilterUploadRequest request = context.getFilterUploadRequest();
        context.setArtifactPath(jar.saveUploadedJar(request.fileBytes(), request.config(), context.getOperationVersion()));
        context.setEvent("create");
    }

    @Override
    public String currentEvent() {
        return "jar-upload";
    }
}
