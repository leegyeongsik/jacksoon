package io.jacksoon.common.util;



import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
public class HttpRequestCheck implements RequestCheck {

    private static final int MAX_HEADER_SIZE = 16 * 1024;
    private static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;

    @Override
    public RequestCheckResult check(
            ByteBuffer inputByteBuffer,
            ByteBuffer accumulationByteBuffer
    ) {
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

            return new RequestCheckResult(false, 0,0);
        }

        int headerLength = headerEnd + 4;

        String header = new String(
                array,
                0,
                headerLength,
                StandardCharsets.US_ASCII
        );

        int contentLength = parseContentLength(header);
        if (contentLength < 0) {
            throw new IllegalStateException("Invalid Content-Length");
        }

        int expectedLength = headerLength + contentLength;

        if (expectedLength > MAX_REQUEST_SIZE) {
            throw new IllegalStateException("Request too large");
        }

        if (totalBytes >= expectedLength) {
            return new RequestCheckResult(true, expectedLength,headerLength);
        }

        return new RequestCheckResult(false, 0,0);
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

    private int parseContentLength(String header) {
        String[] lines = header.split("\r\n");

        for (String line : lines) {
            if (line.regionMatches(
                    true,
                    0,
                    "Content-Length:",
                    0,
                    15
            )) {
                try {
                    return Integer.parseInt(
                            line.substring(15).trim()
                    );
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }

        return 0;
    }
}