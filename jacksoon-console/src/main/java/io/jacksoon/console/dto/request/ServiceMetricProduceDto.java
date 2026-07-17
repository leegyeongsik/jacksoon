package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceMetricProduceDto extends BaseProduceDto {
    private String serviceName;
    private long successCount;
    private long failureCount;
}