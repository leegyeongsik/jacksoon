package io.jacksoon.registry.dto.produce;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegistryRuleProduceDto {
    private String pathPrefix;
    private boolean stripPrefix;
    public RegistryRuleProduceDto(String pathPrefix,  boolean stripPrefix){
        this.pathPrefix =  pathPrefix;
        this.stripPrefix = stripPrefix;
    }
}
