package io.jacksoon.router.produce.dto;

import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FilterMetricProduceDto extends ProduceDto {
    private String filter;
    private long successCount;
    private long failureCount;
}
