package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.common.pipeline.context.HttpRequest;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.pipeline.context.RouterPipelineContext;
import io.jacksoon.router.exception.InvalidRequestException;
import io.jacksoon.router.pipeline.executor.depth.RouterDepth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class HttpParse implements RouterDepth {
    @Override
    public void dodo(RouterPipelineContext context) {
        HttpRequest httpRequest = context.getRequest();

        ByteBuffer buffer = context.getByteBuffer().duplicate();

        int headerLength = context.getByteBufferIndex();
        int requestLength = buffer.limit();

        parseHeader(buffer, headerLength, httpRequest);
        parseBody(buffer, headerLength, requestLength, httpRequest);

    }

    private void parseHeader(ByteBuffer buffer, int headerLength, HttpRequest httpRequest) {
        byte[] headerBytes = new byte[headerLength];

        buffer.position(0);
        buffer.get(headerBytes);

        String header = new String(headerBytes, StandardCharsets.UTF_8);

        String[] lines = header.split("\r\n");

        if (lines.length == 0) {
            throw new InvalidRequestException("Invalid HTTP request line");
        }

        String[] requestLine = lines[0].trim().split(" ");

        if (requestLine.length < 3) {
            throw new InvalidRequestException("HTTP request line must contain method, path and version");
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

            httpRequest.getHeaders().put(parts[0].trim(), parts[1].trim());
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

    @Override
    public String nextEvent() {
        return "router";
    }
}
