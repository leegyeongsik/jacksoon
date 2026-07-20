package io.jacksoon.router.produce.dto;

import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.produce.dto.ProduceHint;
import io.jacksoon.common.produce.dto.ProducerType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor

public class RouterMetricProduceDto extends ProduceDto { // 이걸로 보내서 보낼때마다 저걸로 연산
    private String serviceName;
    private long successCount;
    private long failureCount;
    public RouterMetricProduceDto(String serviceName ,long successCount , long failureCount){
        super(ProduceHint.ROUTER_METRIC, ProducerType.ROUTER, Instant.now());
        this.serviceName = serviceName;
        this.failureCount = failureCount;
        this.successCount = successCount;
    }
}