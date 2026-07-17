package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FilterProduceDto extends BaseProduceDto {
    private FilterAction filterAction;
    private String filterName;
}