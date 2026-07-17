package io.jacksoon.common.produce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Getter
@NoArgsConstructor
public class ProduceDto {
    private ProduceHint hint;
    private ProducerType producerType;
    private Instant occurredAt;
}
