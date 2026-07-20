package io.jacksoon.router.produce.dto;

import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class FilterFailureProduceDto extends ProduceDto {
    private String method;
    private String path;
    private String filter;
    private String timing;
    private Integer statusCode;
    private String reason;
    private String serviceName;
}
