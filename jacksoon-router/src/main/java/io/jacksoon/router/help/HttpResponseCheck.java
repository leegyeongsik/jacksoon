package io.jacksoon.router.help;


import io.jacksoon.init.annotation.Init;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Init
public class HttpResponseCheck implements ResponseCheck {

    private static final int MAX_HEADER_SIZE = 16 * 1024;
    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024;

    @Override
    public ResponseCheckResult check(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer) {
        accumulationByteBuffer.put(inputByteBuffer);

        int totalBytes = accumulationByteBuffer.position();
        if (totalBytes > MAX_RESPONSE_SIZE) {
            throw new IllegalStateException("Response too large");
        }
        byte[] array = accumulationByteBuffer.array();

        int headerEnd = findHeaderEnd(array, totalBytes);

        if (headerEnd == -1) {
            if (totalBytes > MAX_HEADER_SIZE) {
                throw new IllegalStateException("Response header too large");
            }
            return new ResponseCheckResult(false, 0, 0, false);
        }

        int headerLength = headerEnd + 4;

        String header = new String(array, 0, headerLength, StandardCharsets.US_ASCII);

        if (isChunked(header)) {
            throw new IllegalStateException("Chunked response is not supported yet");
        }

        int contentLength = parseContentLength(header);

        if (contentLength < 0) {
            throw new IllegalStateException("Invalid Content-Length");
        }

        int expectedLength = headerLength + contentLength;

        if (expectedLength > MAX_RESPONSE_SIZE) {
            throw new IllegalStateException("Response too large");
        }

        if (totalBytes >= expectedLength) {
            return new ResponseCheckResult(true, expectedLength, headerLength, false);
        }

        return new ResponseCheckResult(false, 0, 0, false);
    }

    @Override
    public ResponseCheckResult eof(ByteBuffer accumulationByteBuffer) {
        int totalBytes = accumulationByteBuffer.position();
        if (totalBytes == 0) {
            return new ResponseCheckResult(false, 0, 0, true);
        }
        byte[] array = accumulationByteBuffer.array();
        int headerEnd = findHeaderEnd(array, totalBytes);
        if (headerEnd == -1) {
            throw new IllegalStateException("Backend closed before response header completed");
        }
        int headerLength = headerEnd + 4;
        return new ResponseCheckResult(true, totalBytes, headerLength, true);
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
            if (line.regionMatches(true, 0, "Content-Length:", 0, 15)) {
                try {
                    return Integer.parseInt(line.substring(15).trim());
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private boolean isChunked(String header) {
        String[] lines = header.split("\r\n");
        for (String line : lines) {
            if (line.regionMatches(true, 0, "Transfer-Encoding:", 0, 18)) {
                return line.toLowerCase().contains("chunked");
            }
        }
        return false;
    }
}