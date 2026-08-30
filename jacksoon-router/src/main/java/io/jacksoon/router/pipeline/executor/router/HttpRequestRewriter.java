package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.InvalidRequestException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class HttpRequestRewriter {

    public ByteBuffer rewritePath(ByteBuffer original, String method, String backendPath, String version) {
        ByteBuffer source = original.duplicate();
        source.position(0);

        int requestLength = source.limit();

        byte[] bytes = new byte[requestLength];
        source.get(bytes);

        int requestLineEnd = findRequestLineEnd(bytes, requestLength);

        if (requestLineEnd == -1) {
            throw new InvalidRequestException("Invalid HTTP request. request line end not found");
        }

        String newRequestLine = method + " " + backendPath + " " + version + "\r\n";
        byte[] newRequestLineBytes = newRequestLine.getBytes(StandardCharsets.US_ASCII);

        int restStart = requestLineEnd + 2;
        int restLength = requestLength - restStart;

        ByteBuffer rewritten = ByteBuffer.allocate(newRequestLineBytes.length + restLength);

        rewritten.put(newRequestLineBytes);
        rewritten.put(bytes, restStart, restLength);
        rewritten.flip();

        return rewritten;
    }

    private int findRequestLineEnd(byte[] bytes, int length) {
        for (int i = 0; i < length - 1; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
}