package io.jacksoon.registry.pipeline.parse;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.registry.pipeline.context.RegistryPipelineContext;
import io.jacksoon.registry.pipeline.depth.RegistryDepth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class RegistryParse implements RegistryDepth {

    @Override
    public void dodo(RegistryPipelineContext context) {
        HttpRequest httpRequest = context.getRequest();

        ByteBuffer buffer = context.getByteBuffer().duplicate();

        int headerLength = context.getByteBufferIndex();
        int requestLength = buffer.limit();

        parseHeader(buffer, headerLength, httpRequest);
        parseBody(buffer, headerLength, requestLength, httpRequest);

        context.setEvent(httpRequest.getPath());
    }

    private void parseHeader(ByteBuffer buffer, int headerLength, HttpRequest httpRequest) {
        byte[] headerBytes = new byte[headerLength];

        buffer.position(0);
        buffer.get(headerBytes);

        String header = new String(headerBytes, StandardCharsets.UTF_8);

        String[] lines = header.split("\r\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("No request line");
        }

        String[] requestLine = lines[0].trim().split(" ");

        if (requestLine.length < 3) {
            throw new IllegalArgumentException("Invalid request line: " + lines[0]);
        }

        httpRequest.setMethod(requestLine[0]);
        httpRequest.setPath(requestLine[1]);
        httpRequest.setVersion(requestLine[2]);

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];

            if (line.isEmpty()) {
                break;
            }

            String[] parts = line.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            httpRequest.getHeaders().put(
                    parts[0].trim(),
                    parts[1].trim()
            );
        }
    }

    private void parseBody(ByteBuffer buffer, int headerLength, int requestLength, HttpRequest httpRequest) {
        int bodyLength = requestLength - headerLength;
        if (bodyLength <= 0) {
            httpRequest.setBody(new byte[]{});
            return;
        }

        byte[] bodyBytes = new byte[bodyLength];

        buffer.position(headerLength);
        buffer.get(bodyBytes);

        httpRequest.setBody(bodyBytes);
    }

    @Override
    public String currentEvent() {
        return "parse";
    }
}