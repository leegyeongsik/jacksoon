package io.jacksoon.console.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceRuleProduceDto {
    private String pathPrefix;
    private boolean stripPrefix;
}
