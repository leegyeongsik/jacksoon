package io.jacksoon.router.connection.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.init.annotation.Init;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

@Init
public class RegistryClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String snapshotUrl = "http://localhost:1013/snapshot";

    public RegistrySnapshot snapshot() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(snapshotUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Registry snapshot request failed. status=" + response.statusCode()
                );
            }

            return objectMapper.readValue(response.body(), RegistrySnapshot.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch registry snapshot", e);
        }
    }
}