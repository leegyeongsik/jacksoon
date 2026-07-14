package io.jacksoon.router.produce.dto;

import io.jacksoon.common.produce.dto.ProduceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public class RouterMetricProduceDto extends ProduceDto { // 이걸로 보내서 보낼때마다 저걸로 연산
    private String serviceName;
    private long successCount;
    private long failureCount;
}