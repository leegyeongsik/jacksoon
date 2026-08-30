package io.jacksoon.registry.connection.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.exception.RegistryConsoleSyncException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Init
public class ConsoleRegistryClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final String snapshotUrl = "http://localhost:1014/registry/snapshot";

    public ConsoleRegistryClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RegistrySnapshot snapshot() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(snapshotUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RegistryConsoleSyncException("Console registry snapshot request failed. status=" + response.statusCode());
            }

            return objectMapper.readValue(response.body(), RegistrySnapshot.class);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RegistryConsoleSyncException("Interrupted while fetching console registry snapshot", e);
        } catch (Exception e) {
            if (e instanceof RegistryConsoleSyncException registryConsoleSyncException) {
                throw registryConsoleSyncException;
            }
            throw new RegistryConsoleSyncException("Failed to fetch console registry snapshot", e);
        }
    }
}
