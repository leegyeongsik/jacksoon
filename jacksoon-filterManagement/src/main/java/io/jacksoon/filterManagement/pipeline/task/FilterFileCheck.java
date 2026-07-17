package io.jacksoon.filterManagement.pipeline.task;

import io.jacksoon.common.filter.FilterFileType;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

@Init
public class FilterFileCheck implements FilterDepth { // 여기서 java파일이면 컴파일로 보내고 // 컴파일이면jar로 보내고 그런식으로
    @Override
    public void dodo(FilterPipelineContext context) {
        FilterFileType fileType = context.getFilterUploadRequest().config().filterFileType();
        context.setEvent(switch (fileType) {
            case JAVA -> "compile";
            case JAR -> "jar-upload";
            case UNKNOWN -> throw new IllegalArgumentException();
        });
    }

    @Override
    public String currentEvent() {
        return "file-check";
    }
}
