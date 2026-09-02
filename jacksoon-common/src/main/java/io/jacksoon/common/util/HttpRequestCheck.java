package io.jacksoon.common.util;

import java.nio.ByteBuffer;

public class HttpRequestCheck implements RequestCheck {

    private static final int MAX_HEADER_SIZE = 16 * 1024;
    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;
    private static final byte[] CONTENT_LENGTH = "Content-Length".getBytes();

    @Override
    public RequestCheckResult check(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer) {
        accumulationByteBuffer.put(inputByteBuffer);
        int totalBytes = accumulationByteBuffer.position();
        if (totalBytes > MAX_REQUEST_SIZE) {
            throw new IllegalStateException("Request too large");
        }
        byte[] array = accumulationByteBuffer.array();
        int headerEnd = findHeaderEnd(array, totalBytes);
        if (headerEnd == -1) {
            if (totalBytes > MAX_HEADER_SIZE) {
                throw new IllegalStateException("Header too large");
            }
            return new RequestCheckResult(false, 0, 0);
        }
        int headerLength = headerEnd + 4;
        int contentLength = parseContentLength(array, headerEnd);
        if (contentLength < 0) {
            throw new IllegalStateException("Invalid Content-Length");
        }
        int expectedLength = headerLength + contentLength;
        if (expectedLength > MAX_REQUEST_SIZE) {
            throw new IllegalStateException("Request too large");
        }
        if (totalBytes >= expectedLength) {
            return new RequestCheckResult(true, expectedLength, headerLength);
        }
        return new RequestCheckResult(false, 0, 0);
    }

    private int findHeaderEnd(byte[] array, int length) {
        for (int i = 0; i <= length - 4; i++) {
            if (array[i] == '\r'
                    && array[i + 1] == '\n'
                    && array[i + 2] == '\r'
                    && array[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private int parseContentLength(byte[] array, int headerEnd) {
        int index = findCrlf(array, 0, headerEnd + 2);
        if (index == -1) {
            return -1;
        }
        index += 2;
        while (index < headerEnd) {
            int lineEnd = findCrlf(array, index, headerEnd + 2);
            if (lineEnd == -1) {
                return -1;
            }
            int colon = findColon(array, index, lineEnd);
            if (colon != -1 && equalsIgnoreCase(array, index, colon, CONTENT_LENGTH)) {
                return parsePositiveInt(array, colon + 1, lineEnd);
            }
            index = lineEnd + 2;
        }
        return 0;
    }
    private int parsePositiveInt(byte[] array, int start, int end) {
        while (start < end && isWhitespace(array[start])) {
            start++;
        }
        while (end > start && isWhitespace(array[end - 1])) {
            end--;
        }
        if (start >= end) {
            return -1;
        }
        long value = 0L;
        for (int i = start; i < end; i++) {
            byte b = array[i];
            if (b < '0' || b > '9') {
                return -1;
            }
            value = value * 10 + (b - '0');
            if (value > Integer.MAX_VALUE) {
                return -1;
            }
        }
        return (int) value;
    }
    private boolean equalsIgnoreCase(byte[] array, int start, int end, byte[] expected) {
        int length = end - start;
        if (length != expected.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            byte actual = toLowerAscii(array[start + i]);
            byte expectedByte = toLowerAscii(expected[i]);
            if (actual != expectedByte) {
                return false;
            }
        }
        return true;
    }
    private byte toLowerAscii(byte value) {
        if (value >= 'A' && value <= 'Z') {
            return (byte) (value + ('a' - 'A'));
        }
        return value;
    }
    private int findCrlf(byte[] array, int start, int end) {
        for (int i = start; i < end - 1; i++) {
            if (array[i] == '\r' && array[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
    private int findColon(byte[] array, int start, int end) {
        for (int i = start; i < end; i++) {
            if (array[i] == ':') {
                return i;
            }
        }
        return -1;
    }
    private boolean isWhitespace(byte value) {
        return value == ' ' || value == '\t';
    }
}