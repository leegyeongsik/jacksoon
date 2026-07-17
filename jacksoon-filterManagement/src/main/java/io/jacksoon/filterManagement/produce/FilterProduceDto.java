package io.jacksoon.filterManagement.produce;

import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FilterProduceDto extends ProduceDto {
    FilterAction filterAction;
    List<FilterStatusDto> currentFilter;
}
