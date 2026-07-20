package io.jacksoon.router.produce.dto;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
public class FilterMetricProduceDto extends ProduceDto {
    private String filterName;
    private long successCount;
    private long failureCount;
    public FilterMetricProduceDto(String filterName , long successCount , long failureCount){
        super(ProduceHint.FILTER_METRIC, ProducerType.ROUTER, Instant.now());
        this.filterName = filterName;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }
}
