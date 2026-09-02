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
        if (source.hasArray()) {
            byte[] bytes = source.array();
            int offset = source.arrayOffset();
            int requestLineEnd = findRequestLineEnd(bytes, offset, offset + requestLength);
            if (requestLineEnd == -1) {
                throw new InvalidRequestException("Invalid HTTP request. request line end not found");
            }
            int firstSpace = findSpace(bytes, offset, requestLineEnd);
            if (firstSpace == -1) {
                throw new InvalidRequestException("Invalid HTTP request. method separator not found");
            }
            int secondSpace = findSpace(bytes, firstSpace + 1, requestLineEnd
            );
            if (secondSpace == -1) {
                throw new InvalidRequestException("Invalid HTTP request. version separator not found");
            }
            if (equalsAscii(bytes, firstSpace + 1, secondSpace, backendPath)) {
                ByteBuffer passthrough = original.duplicate();
                passthrough.position(0);
                return passthrough;
            }
            byte[] backendPathBytes = backendPath.getBytes(StandardCharsets.US_ASCII);
            int prefixLength = firstSpace - offset + 1;
            int suffixOffset = secondSpace - offset;
            int suffixLength = requestLength - suffixOffset;
            ByteBuffer rewritten = ByteBuffer.allocate(prefixLength + backendPathBytes.length + suffixLength);
            rewritten.put(bytes, offset, prefixLength);
            rewritten.put(backendPathBytes);
            rewritten.put(bytes, offset + suffixOffset, suffixLength);
            rewritten.flip();
            return rewritten;
        }
        byte[] bytes = new byte[requestLength];
        source.get(bytes);
        int requestLineEnd = findRequestLineEnd(bytes, 0, requestLength);
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
    private int findRequestLineEnd(byte[] bytes, int start, int end) {
        for (int i = start; i < end - 1; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
    private int findSpace(byte[] bytes, int start, int end) {
        for (int i = start; i < end; i++) {
            if (bytes[i] == ' ') {
                return i;
            }
        }
        return -1;
    }
    private boolean equalsAscii(byte[] bytes, int start, int end, String value) {
        int length = end - start;
        if (length != value.length()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if ((bytes[start + i] & 0xff) != value.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}