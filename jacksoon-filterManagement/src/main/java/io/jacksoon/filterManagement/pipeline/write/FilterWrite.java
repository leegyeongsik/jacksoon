package io.jacksoon.filterManagement.pipeline.write;

import io.jacksoon.common.handler.IOStore;
import io.jacksoon.common.pipeline.context.HttpResponse;
import io.jacksoon.common.util.ResponseContext;
import io.jacksoon.filterManagement.exception.InvalidFilterResponseException;
import io.jacksoon.filterManagement.pipeline.context.FilterPipelineContext;
import io.jacksoon.filterManagement.pipeline.depth.FilterDepth;
import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Init
public class FilterWrite implements FilterDepth {
    private final IOStore ioStore;
    public FilterWrite(IOStore ioStore) {
        this.ioStore = ioStore;
    }
    @Override
    public void dodo(FilterPipelineContext context) {
        HttpResponse response = context.getResponse();
        validateResponse(response);
        ByteBuffer body = response.getWriteBuffer();
        if (body == null) {
            body = ByteBuffer.wrap(new byte[0]);
        } else {
            body = body.duplicate();
        }

        response.addHeader("Content-Length", String.valueOf(body.remaining()));
        response.addHeader("Connection", "close");
        byte[] headerBytes = createResponseHeader(response).getBytes(StandardCharsets.UTF_8);
        ByteBuffer output = ByteBuffer.allocate(headerBytes.length + body.remaining());
        output.put(headerBytes);
        output.put(body);
        output.flip();

        SelectionKey key = context.getSelectionKey();
        AtomicInteger current = context.getCurrent();
        if (key == null || current == null) {
            return;
        }
        context.setCloseAfterWrite(true);
        ioStore.offer(key, new ResponseContext(current.get(), output, true));
        context.setEvent(null);
    }

    private void validateResponse(HttpResponse response) {
        if (response == null) {
            throw new InvalidFilterResponseException("HttpResponse is null");
        }

        int statusCode = response.getStatusCode();
        if (statusCode < 100 || statusCode > 599) {
            throw new InvalidFilterResponseException("Invalid HTTP response status: " + statusCode + " " + response.getReasonPhrase());
        }
        if (response.getReasonPhrase() == null) {
            throw new InvalidFilterResponseException("HTTP reason phrase is null. statusCode=" + statusCode);
        }
    }
    private String createResponseHeader(HttpResponse response) {
        StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 ")
                .append(response.getStatusCode())
                .append(' ')
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

    @Override
    public String currentEvent() {
        return "write";
    }
}