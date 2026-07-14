package io.jacksoon.common.produce.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ProduceWorker<T extends ProduceDto> implements Runnable {
    protected final int batchSize = 500;
    protected final List<T> buffer = new ArrayList<>();
    protected final CommonBlockingQueue<T> queue;
    private final HttpClient httpClient;
    private final String path;
    private final ObjectMapper objectMapper;

    public ProduceWorker(CommonBlockingQueue<T> queue, String path, ObjectMapper objectMapper) {
        this.queue = queue;
        this.httpClient = HttpClient.newBuilder().build();
        this.path = path;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                T dto = queue.poll();
                if (dto != null) {
                    buffer.add(dto);
                }
                boolean sizeTrigger = buffer.size() >= batchSize;
                boolean timeoutTrigger = dto == null && !buffer.isEmpty();
                if (sizeTrigger || timeoutTrigger) {
                    flush();
                }
            }
        } catch (Exception e) {
        } finally {
            flush();
        }
    }
    protected void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        List<T> batch = new ArrayList<>(buffer);

        saveAll(batch);

        buffer.subList(0, batch.size()).clear();
    }
    public void saveAll(List<T> batch) {
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
