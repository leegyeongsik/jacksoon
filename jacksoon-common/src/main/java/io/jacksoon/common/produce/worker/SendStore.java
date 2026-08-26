package io.jacksoon.common.produce.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.produce.dto.ProduceDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SendStore implements ProduceStore<ProduceDto>{
    private final ObjectMapper objectMapper;
    private final String path;
    private final HttpClient httpClient;

    public SendStore(ObjectMapper objectMapper, String path, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.path = path;
        this.httpClient = httpClient;
    }

    @Override
    public void saveAll(List<ProduceDto> batch) {
        try {
            String jsonBody = objectMapper.writeValueAsString(batch);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException();
            }
        } catch (IOException e) {
            throw new IllegalStateException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
    }
}
