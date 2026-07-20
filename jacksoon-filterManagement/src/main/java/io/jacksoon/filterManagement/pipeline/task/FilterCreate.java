package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.filterManagement.pipeline.util.Jar;
import io.jacksoon.filterManagement.store.FilterDefinition;
import io.jacksoon.filterManagement.store.FilterStore;
import io.jacksoon.init.annotation.Init;

import java.nio.file.Files;

@Init
public class FilterCreate implements FilterDepth {
    @Override
    public void dodo(FilterPipelineContext context) {
        FilterConfigDto config = context.getFilterUploadRequest().config();
        if (context.getArtifactPath() == null || !Files.isRegularFile(context.getArtifactPath())) {
            throw new IllegalStateException();
        }
        context.getCandidateFilters().put(config.filterName(), new FilterDefinition(config, context.getOperationVersion(), context.getArtifactPath()));
        context.setEvent("bundle");
    }

    @Override
    public String currentEvent() {
        return "create";
    }
}
