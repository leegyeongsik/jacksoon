package io.jacksoon.filterManagement.produce;

import io.jacksoon.common.filter.FilterTiming;
import io.jacksoon.common.filter.PipelineType;

public record FilterStatusDto(
        String filterName,
        String className,
        FilterTiming timing,
        PipelineType pipeline,
        int order

) {
}