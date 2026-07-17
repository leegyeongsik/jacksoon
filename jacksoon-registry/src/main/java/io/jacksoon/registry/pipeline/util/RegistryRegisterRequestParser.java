package io.jacksoon.registry.pipeline.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.dto.request.EndpointInfo;
import io.jacksoon.registry.dto.request.RegistryRegisterRequest;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;

import java.nio.charset.StandardCharsets;

@Init
public class RegistryRegisterRequestParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegistryRegisterRequest parse(RegistryPipelineContext context) {
        String body = new String(context.getRequest().getBody(), StandardCharsets.UTF_8);
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Register request body is empty");
        }
        try {
            RegistryRegisterRequest request =
                    objectMapper.readValue(body, RegistryRegisterRequest.class);

            validate(request);

            return request;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid register request body", e);
        }
    }

    private void validate(RegistryRegisterRequest request) {
        if (request.getServiceName() == null || request.getServiceName().isBlank()) {
            throw new IllegalArgumentException("serviceName is required");
        }

        if (request.getInstanceId() == null || request.getInstanceId().isBlank()) {
            throw new IllegalArgumentException("instanceId is required");
        }

        if (request.getEndpoint() == null) {
            throw new IllegalArgumentException("endpoints is required");
        }

        if (request.getRules() == null || request.getRules().isEmpty()) {
            throw new IllegalArgumentException("rules is required");
        }

        EndpointInfo endpoint = request.getEndpoint();
        if (endpoint.getHost() == null || endpoint.getHost().isBlank()) {
            throw new IllegalArgumentException("endpoint.host is required");
        }

        if (endpoint.getPort() <= 0) {
            throw new IllegalArgumentException("endpoint.port is invalid");
        }

        if (endpoint.getProtocol() == null || endpoint.getProtocol().isBlank()) {
            endpoint.setProtocol("http");
        }

        if (endpoint.getHealthPath() == null || endpoint.getHealthPath().isBlank()) {
            endpoint.setHealthPath("/actuator/health");
        }
        request.getRules().forEach(rule -> {
            if (rule.getPathPrefix() == null || rule.getPathPrefix().isBlank()) {
                throw new IllegalArgumentException("rule.pathPrefix is required");
            }

            if (!rule.getPathPrefix().startsWith("/")) {
                throw new IllegalArgumentException("rule.pathPrefix must start with /");
            }
        });
    }
}