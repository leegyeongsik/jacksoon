package io.jacksoon.console.dto.response;

import io.jacksoon.console.type.FilterTiming;
import io.jacksoon.console.type.PipelineType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FilterResponseDto {
    private Long pipelineTimingId;
    private PipelineType pipeline;
    private FilterTiming timing;
    private List<FilterInfoDto> filters;
}
