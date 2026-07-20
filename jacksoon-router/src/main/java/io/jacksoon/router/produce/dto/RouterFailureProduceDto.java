package io.jacksoon.router.produce.dto;

import io.jacksoon.common.filter.PipelineType;
import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RouterFailureProduceDto extends ProduceDto {
    private String method;
    private String path;
    private PipelineType pipeline;
    private RouterFailureStage stage;
    private RouterFailureType failureType;
    private Integer responseStatusCode;
    private String reason;
    private String serviceName;
}