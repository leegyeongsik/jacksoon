package io.jacksoon.registry.pipeline.write;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.common.util.ResponseContext;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Init
public class RegistryWrite implements RegistryDepth {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IOStore ioStore;

    public RegistryWrite(IOStore ioStore) {
        this.ioStore = ioStore;
    }

    @Override
    public void dodo(RegistryPipelineContext context) {
        HttpResponse httpResponse = context.getResponse();

        String body = toJson(httpResponse.getBody());
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        httpResponse.addHeader("Content-Type", "application/json");
        httpResponse.addHeader("Content-Length", String.valueOf(bodyBytes.length));
        httpResponse.addHeader("Connection", "keep-alive");

        String responseHeader = createResponseHeader(httpResponse);
        byte[] headerBytes = responseHeader.getBytes(StandardCharsets.UTF_8);

        ByteBuffer responseBuffer = ByteBuffer.allocate(headerBytes.length + bodyBytes.length);
        responseBuffer.put(headerBytes);
        responseBuffer.put(bodyBytes);
        responseBuffer.flip();

        SelectionKey selectionKey = context.getSelectionKey();
        AtomicInteger current = context.getCurrent();
        if (selectionKey == null || current == null) {
            return;
        }

        ioStore.offer(selectionKey, new ResponseContext(current.get(), responseBuffer, context.isCloseAfterWrite()));
        context.setEvent(null);
    }

    private String createResponseHeader(HttpResponse response) {
        StringBuilder builder = new StringBuilder();

        builder.append("HTTP/1.1 ")
                .append(response.getStatusCode())
                .append(" ")
                .append(response.getReasonPhrase())
                .append("\r\n");

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            builder.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        builder.append("\r\n");
        return builder.toString();
    }

    private String toJson(Object body) {
        if (body == null) {
            return "";
        }

        if (body instanceof String stringBody) {
            return stringBody;
        }

        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response body", e);
        }
    }

    @Override
    public String currentEvent() {
        return "write";
    }
}
