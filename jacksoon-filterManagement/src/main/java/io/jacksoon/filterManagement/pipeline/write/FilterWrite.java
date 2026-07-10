package io.jacksoon.filterManagement.pipeline.write;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterWrite implements FilterDepth {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String currentEvent() {
        return "write";
    }

    @Override
    public void dodo(FilterPipelineContext context) {

    }
}