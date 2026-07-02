package io.jacksoon.registry.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RegistryRegisterRequest {
    private String serviceName;
    private String instanceId;
    private EndpointInfo endpoint;
    private List<RouteRule> rules;
    private Map<String, String> metadata;
}