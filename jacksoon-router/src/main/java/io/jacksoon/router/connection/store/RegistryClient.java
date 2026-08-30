package io.jacksoon.router.connection.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.registry.dto.response.RegistrySnapshot;
import io.jacksoon.common.registry.dto.response.RegistryVersionResponse;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.RouterRegistryException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Init
public class RegistryClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    private final String versionUrl = "http://localhost:1013/version";
    private final String snapshotUrl = "http://localhost:1013/snapshot";

    public RegistryClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public long version() {
        RegistryVersionResponse response = get(versionUrl, RegistryVersionResponse.class);
        return response.getVersion();
    }
    public RegistrySnapshot snapshot() {
        return get(snapshotUrl, RegistrySnapshot.class);
    }
    private <T> T get(String url, Class<T> responseType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RouterRegistryException(
                        "Registry request failed. url=" + url + ", status=" + response.statusCode()
                );
            }

            return objectMapper.readValue(response.body(), responseType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouterRegistryException("Interrupted while requesting registry", e);
        } catch (Exception e) {
            if (e instanceof RouterRegistryException routerRegistryException) {
                throw routerRegistryException;
            }
            throw new RouterRegistryException("Failed to request registry. url=" + url, e);
        }
    }
}