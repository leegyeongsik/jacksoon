package io.jacksoon.console.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceRuleDto {
    long serviceRuleId;
    private String pathPrefix;
    private boolean stripPrefix;
}
