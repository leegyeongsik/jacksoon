package io.jacksoon.registry.pipeline.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.request.EndpointInfo;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.dto.request.RouteRule;
import io.jacksoon.registry.exception.InvalidRegistryRequestException;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Init
public class RegistryRegisterRequestParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegistryRegisterRequest parse(RegistryPipelineContext context) {
        if (context == null || context.getRequest() == null || context.getRequest().getBody() == null) {
            throw new InvalidRegistryRequestException("Register request body is empty");
        }
        String body = new String(context.getRequest().getBody(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            throw new InvalidRegistryRequestException("Register request body is empty");
        }
        try {
            RegistryRegisterRequest request = objectMapper.readValue(body, RegistryRegisterRequest.class);
            validate(request);
            return request;
        } catch (JsonProcessingException e) {
            throw new InvalidRegistryRequestException("Invalid register request body", e);
        }
    }

    private void validate(RegistryRegisterRequest request) {
        if (request == null) {
            throw new InvalidRegistryRequestException("Register request is null");
        }
        if (request.getServiceName() == null || request.getServiceName().isBlank()) {
            throw new InvalidRegistryRequestException("serviceName is required");
        }
        if (request.getServiceName().contains(":")) {
            throw new InvalidRegistryRequestException("serviceName must not contain ':'");
        }
        if (request.getInstanceId() == null || request.getInstanceId().isBlank()) {
            throw new InvalidRegistryRequestException("instanceId is required");
        }
        if (request.getInstanceId().contains(":")) {
            throw new InvalidRegistryRequestException("instanceId must not contain ':'");
        }
        if (request.getEndpoint() == null) {
            throw new InvalidRegistryRequestException("endpoint is required");
        }
        if (request.getRules() == null || request.getRules().isEmpty()) {
            throw new InvalidRegistryRequestException("rules is required");
        }

        EndpointInfo endpoint = request.getEndpoint();
        if (endpoint.getHost() == null || endpoint.getHost().isBlank()) {
            throw new InvalidRegistryRequestException("endpoint.host is required");
        }
        if (endpoint.getPort() <= 0 || endpoint.getPort() > 65535) {
            throw new InvalidRegistryRequestException("endpoint.port is invalid");
        }
        if (endpoint.getProtocol() == null || endpoint.getProtocol().isBlank()) {
            endpoint.setProtocol("http");
        }
        if (!"http".equalsIgnoreCase(endpoint.getProtocol())) {
            throw new InvalidRegistryRequestException("Only http endpoint protocol is supported");
        }
        endpoint.setProtocol("http");
        if (endpoint.getHealthPath() == null || endpoint.getHealthPath().isBlank()) {
            endpoint.setHealthPath("/actuator/health");
        }
        if (!endpoint.getHealthPath().startsWith("/")) {
            throw new InvalidRegistryRequestException("endpoint.healthPath must start with /");
        }

        Set<String> pathPrefixes = new HashSet<>();
        for (RouteRule rule : request.getRules()) {
            if (rule == null) {
                throw new InvalidRegistryRequestException("rule must not be null");
            }
            if (rule.getPathPrefix() == null || rule.getPathPrefix().isBlank()) {
                throw new InvalidRegistryRequestException("rule.pathPrefix is required");
            }
            if (!rule.getPathPrefix().startsWith("/")) {
                throw new InvalidRegistryRequestException("rule.pathPrefix must start with /");
            }
            if (!pathPrefixes.add(rule.getPathPrefix())) {
                throw new InvalidRegistryRequestException("Duplicate rule.pathPrefix: " + rule.getPathPrefix());
            }
        }
    }
}
