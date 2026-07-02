package io.jacksoon.registry.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RouteRule {
    private String pathPrefix;
    private boolean stripPrefix;
}