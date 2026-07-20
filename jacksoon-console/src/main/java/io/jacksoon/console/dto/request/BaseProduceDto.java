package io.jacksoon.console.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "hint",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServiceProduceDto.class, name = "SERVICE"),
        @JsonSubTypes.Type(value = ServiceRuleProduceRequestDto.class, name = "SERVICE_RULE"),
        @JsonSubTypes.Type(value = FilterProduceDto.class, name = "FILTER"),
        @JsonSubTypes.Type(value = ServiceMetricProduceDto.class, name = "ROUTER_METRIC"),
        @JsonSubTypes.Type(value = FilterMetricProduceDto.class, name = "FILTER_METRIC")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseProduceDto {
    private ProduceHint hint;
    private ProducerType producerType;
    private Instant occurredAt;
}