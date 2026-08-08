package io.jacksoon.registry.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistryRegisterRequest {
    private String serviceName;
    private String instanceId;
    private EndpointInfo endpoint;
    private List<RouteRule> rules;
}