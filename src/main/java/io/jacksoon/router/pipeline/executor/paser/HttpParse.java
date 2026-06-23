package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.router.init.annotation.Init;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.context.RouterRequest;
import io.jacksoon.router.pipeline.executor.Depth;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class HttpParse implements Depth {
    @Override
    public void dodo(PipelineContext context) {

        RouterRequest routerRequest = context.getRequest();

        int headerLength = context.getByteBufferIndex();

        byte[] bytes = new byte[headerLength];

        ByteBuffer buffer = context.getByteBuffer().duplicate();

        buffer.position(0);
        buffer.get(bytes);

        String header = new String(bytes, StandardCharsets.UTF_8);

        String[] lines = header.split("\r\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("No request line");
        }

        String[] requestLine = lines[0].trim().split(" ");

        if (requestLine.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid request line: [" + lines[0] + "]"
            );
        }

        routerRequest.setMethod(requestLine[0]);
        routerRequest.setPath(requestLine[1]);
        routerRequest.setVersion(requestLine[2]);

        for (int i = 1; i < lines.length; i++) {

            String line = lines[i];

            if (line.isEmpty()) {
                break;
            }

            String[] parts = line.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            routerRequest.getHeaders().put(
                    parts[0].trim(),
                    parts[1].trim()
            );
        }
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
