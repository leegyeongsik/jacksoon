package io.jacksoon.filterManagement.pipeline.context;

public record FilterConfigDto(
        String filterName,
        FilterTiming timing,
        PipelineType pipeline,
        String path,
        int order,
        FilterFileType filterFileType
) {
}