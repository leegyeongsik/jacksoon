package io.jacksoon.filterManagement.produce;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Getter
@NoArgsConstructor
public class FilterProduceDto extends ProduceDto {
    private FilterAction filterAction;
    private String filterName;
    public FilterProduceDto(FilterAction filterAction , String filterName){
        super(ProduceHint.FILTER, ProducerType.FILTER_MANAGEMENT, Instant.now());
        this.filterAction = filterAction;
        this.filterName = filterName;
    }
}