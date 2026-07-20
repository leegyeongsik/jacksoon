package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FilterMetricProduceDto extends BaseProduceDto { // 필터를 등록할떄 이름을 timing_pipeline_fitler행위 이렇게 저장하자
    private String filterName;
    private long successCount;
    private long failureCount;
}