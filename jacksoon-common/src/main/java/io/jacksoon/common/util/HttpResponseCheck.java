package io.jacksoon.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class HttpResponseCheck implements ResponseCheck {

    private static final int MAX_HEADER_SIZE = 16 * 1024;
    private static final int MAX_TRAILER_SIZE = 16 * 1024;
    private static final int MAX_CHUNK_LINE_SIZE = 1024;
    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024;
    @Override
    public ResponseCheckResult check(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer) {
        append(inputByteBuffer, accumulationByteBuffer);
        return inspect(accumulationByteBuffer, false);
    }
    @Override
    public ResponseCheckResult eof(ByteBuffer accumulationByteBuffer) { //
        return inspect(accumulationByteBuffer, true);
    }
    private void append(ByteBuffer inputByteBuffer, ByteBuffer accumulationByteBuffer) {
        int incomingBytes = inputByteBuffer.remaining();
        int currentBytes = accumulationByteBuffer.position();
        if ((long) currentBytes + incomingBytes > MAX_RESPONSE_SIZE) {
            throw new IllegalStateException("Response too large");
        }
        if (incomingBytes > accumulationByteBuffer.remaining()) {
            throw new IllegalStateException("Response buffer capacity exceeded: capacity=" + accumulationByteBuffer.capacity());
        }
        accumulationByteBuffer.put(inputByteBuffer);
    }

    private ResponseCheckResult inspect(ByteBuffer accumulationByteBuffer, boolean eof) {
        int totalBytes = accumulationByteBuffer.position();
        if (totalBytes == 0) {
            return new ResponseCheckResult(false, 0, 0, eof, eof);
        }

        if (totalBytes > MAX_RESPONSE_SIZE) {
            throw new IllegalStateException("Response too large");
        }

        if (!accumulationByteBuffer.hasArray()) {
            throw new IllegalStateException(
                    "HttpResponseCheck requires a heap ByteBuffer"
            );
        }

        byte[] array = accumulationByteBuffer.array();
        int arrayStart = accumulationByteBuffer.arrayOffset();
        int arrayEnd = arrayStart + totalBytes;

        int headerEnd = findHeaderEnd(array, arrayStart, arrayEnd);

        if (headerEnd == -1) {
            if (totalBytes > MAX_HEADER_SIZE) {
                throw new IllegalStateException("Response header too large");
            }
            if (eof) {
                throw new IllegalStateException("Backend closed before response header completed");
            }
            return ResponseCheckResult.incomplete();
        }

        int absoluteHeaderEnd = headerEnd + 4;
        int headerLength = absoluteHeaderEnd - arrayStart;
        if (headerLength > MAX_HEADER_SIZE) {
            throw new IllegalStateException("Response header too large");
        }
        String header = new String(array, arrayStart, headerLength, StandardCharsets.US_ASCII);
        int statusCode = parseStatusCode(header);
        if (statusCode >= 100 && statusCode < 200) {
            throw new IllegalStateException("Informational response is not supported yet: " + statusCode);
        }
        boolean connectionClose = shouldCloseConnection(header);

        if (statusCode == 204 || statusCode == 304) {
            return new ResponseCheckResult(true, headerLength, headerLength, false, connectionClose);
        }
        boolean hasTransferEncoding = hasHeader(header, "Transfer-Encoding");
        boolean chunked = hasHeaderToken(header, "Transfer-Encoding", "chunked");
        int contentLength = parseContentLength(header);
        if (hasTransferEncoding && contentLength >= 0) {
            throw new IllegalStateException("Both Transfer-Encoding and Content-Length are present");
        }
        if (chunked) {
            validateChunkedIsFinalEncoding(header);
            int chunkedEnd = findChunkedEnd(array, absoluteHeaderEnd, arrayEnd);
            if (chunkedEnd == -1) {
                if (eof) {
                    throw new IllegalStateException("Backend closed before chunked response completed");
                }
                return new ResponseCheckResult(false, 0, headerLength, false, connectionClose);
            }
            int responseLength = chunkedEnd - arrayStart;
            return new ResponseCheckResult(true, responseLength, headerLength, false, connectionClose);
        }
        if (hasTransferEncoding) {
            if (!eof) {
                return new ResponseCheckResult(false, 0, headerLength, true, true);
            }
            return new ResponseCheckResult(true, totalBytes, headerLength, true, true);
        }
        if (contentLength >= 0) {
            long expectedLength = (long) headerLength + contentLength;
            if (expectedLength > MAX_RESPONSE_SIZE) {
                throw new IllegalStateException("Response too large");
            }
            if (totalBytes < expectedLength) {
                if (eof) {
                    throw new IllegalStateException("Backend closed before Content-Length body completed" + ": expected=" + expectedLength + ", actual=" + totalBytes);
                }
                return new ResponseCheckResult(false, 0, headerLength, false, connectionClose);
            }
            return new ResponseCheckResult(true, (int) expectedLength, headerLength, false, connectionClose);
        }
        if (!eof) {
            return new ResponseCheckResult(false, 0, headerLength, true, true);
        }
        return new ResponseCheckResult(true, totalBytes, headerLength, true, true);
    }
    private int findChunkedEnd(byte[] array, int bodyStart, int arrayEnd) {
        int position = bodyStart;
        while (true) {
            int chunkSizeLineEnd = findCrlf(array, position, arrayEnd);
            if (chunkSizeLineEnd == -1) {
                if (arrayEnd - position > MAX_CHUNK_LINE_SIZE) {
                    throw new IllegalStateException("Chunk size line too large");
                }
                return -1;
            }
            if (chunkSizeLineEnd - position > MAX_CHUNK_LINE_SIZE) {
                throw new IllegalStateException("Chunk size line too large");
            }
            long chunkSize = parseChunkSize(array, position, chunkSizeLineEnd);
            int chunkDataStart = chunkSizeLineEnd + 2;
            if (chunkSize == 0) {
                return findChunkTrailerEnd(array, chunkDataStart, arrayEnd);
            }

            long chunkDataEndLong = (long) chunkDataStart + chunkSize;

            if (chunkDataEndLong + 2 > MAX_RESPONSE_SIZE) {
                throw new IllegalStateException("Response too large");
            }

            if (chunkDataEndLong + 2 > arrayEnd) {
                return -1;
            }
            int chunkDataEnd = (int) chunkDataEndLong;
            if (array[chunkDataEnd] != '\r' || array[chunkDataEnd + 1] != '\n') {
                throw new IllegalStateException("Invalid chunk data terminator");
            }
            position = chunkDataEnd + 2;
        }
    }

    private int findChunkTrailerEnd(byte[] array, int trailerStart, int arrayEnd) {
        if (trailerStart + 2 <= arrayEnd && array[trailerStart] == '\r' && array[trailerStart + 1] == '\n') {
            return trailerStart + 2;
        }
        int trailerEnd = findHeaderEnd(array, trailerStart, arrayEnd);
        if (trailerEnd == -1) {
            if (arrayEnd - trailerStart > MAX_TRAILER_SIZE) {
                throw new IllegalStateException("Chunk trailer too large");
            }
            return -1;
        }
        return trailerEnd + 4;
    }

    private long parseChunkSize(byte[] array, int start, int end) {
        String chunkLine = new String(array, start, end - start, StandardCharsets.US_ASCII);
        int extensionIndex = chunkLine.indexOf(';');
        String sizeText = extensionIndex >= 0 ? chunkLine.substring(0, extensionIndex).trim() : chunkLine.trim();
        if (sizeText.isEmpty()) {
            throw new IllegalStateException("Empty chunk size");
        }
        for (int i = 0; i < sizeText.length(); i++) {
            if (Character.digit(sizeText.charAt(i), 16) == -1) {
                throw new IllegalStateException("Invalid chunk size: " + sizeText
                );
            }
        }

        try {
            long chunkSize = Long.parseLong(sizeText, 16);
            if (chunkSize > MAX_RESPONSE_SIZE) {
                throw new IllegalStateException("Response too large");
            }
            return chunkSize;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid chunk size: " + sizeText, e);
        }
    }

    private int parseStatusCode(String header) {
        int lineEnd = header.indexOf("\r\n");
        String statusLine = lineEnd >= 0 ? header.substring(0, lineEnd) : header;
        String[] parts = statusLine.trim().split("\\s+", 3);
        if (parts.length < 2 || !parts[0].startsWith("HTTP/")) {
            throw new IllegalStateException("Invalid HTTP status line: " + statusLine);
        }
        try {
            int statusCode = Integer.parseInt(parts[1]);
            if (statusCode < 100 || statusCode > 999) {
                throw new IllegalStateException("Invalid HTTP status code: " + statusCode);
            }
            return statusCode;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid HTTP status line: " + statusLine, e
            );
        }
    }

    private int parseContentLength(String header) {
        Integer parsedContentLength = null;
        String[] lines = header.split("\r\n");
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                continue;
            }
            String headerName = line.substring(0, colonIndex).trim();
            if (!headerName.equalsIgnoreCase("Content-Length")) {
                continue;
            }
            String rawValue = line.substring(colonIndex + 1).trim();
            String[] values = rawValue.split(",");
            for (String value : values) {
                int current;

                try {
                    current = Integer.parseInt(value.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid Content-Length: " + rawValue, e);
                }
                if (current < 0) {
                    throw new IllegalStateException("Negative Content-Length: " + current);
                }
                if (parsedContentLength != null && parsedContentLength != current) {
                    throw new IllegalStateException("Conflicting Content-Length values");
                }
                parsedContentLength = current;
            }
        }

        return parsedContentLength == null ? -1 : parsedContentLength;
    }
    private boolean shouldCloseConnection(String header) {
        if (hasHeaderToken(header, "Connection", "close")) {
            return true;
        }

        String statusLine = firstLine(header);
        if (statusLine.startsWith("HTTP/1.0")) {
            return !hasHeaderToken(header, "Connection", "keep-alive");
        }
        return false;
    }
    private void validateChunkedIsFinalEncoding(String header) {
        String transferEncoding = findHeaderValue(header, "Transfer-Encoding");
        if (transferEncoding == null) {
            throw new IllegalStateException("Missing Transfer-Encoding header");
        }
        String[] encodings = transferEncoding.split(",");
        String finalEncoding = encodings[encodings.length - 1].trim().toLowerCase(Locale.ROOT);
        if (!finalEncoding.equals("chunked")) {
            throw new IllegalStateException("Chunked must be the final transfer encoding");
        }
    }

    private boolean hasHeader(String header, String name) {
        return findHeaderValue(header, name) != null;
    }
    private boolean hasHeaderToken(String header, String headerName, String expectedToken) {
        String headerValue = findHeaderValue(header, headerName);
        if (headerValue == null) {
            return false;
        }
        String[] tokens = headerValue.split(",");
        for (String token : tokens) {
            if (token.trim().equalsIgnoreCase(expectedToken)) {
                return true;
            }
        }
        return false;
    }

    private String findHeaderValue(String header, String expectedName) {
        String[] lines = header.split("\r\n");
        StringBuilder result = null;
        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex <= 0) {
                continue;
            }
            String headerName = line.substring(0, colonIndex).trim();
            if (!headerName.equalsIgnoreCase(expectedName)) {
                continue;
            }
            if (result == null) {
                result = new StringBuilder();
            } else {
                result.append(',');
            }
            result.append(line.substring(colonIndex + 1).trim());
        }
        return result == null ? null : result.toString();
    }

    private String firstLine(String header) {
        int lineEnd = header.indexOf("\r\n");

        return lineEnd == -1 ? header : header.substring(0, lineEnd);
    }

    private int findHeaderEnd(byte[] array, int start, int end) {
        for (int i = start; i <= end - 4; i++) {
            if (array[i] == '\r' && array[i + 1] == '\n' && array[i + 2] == '\r' && array[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
    private int findCrlf(byte[] array, int start, int end) {
        for (int i = start; i <= end - 2; i++) {
            if (array[i] == '\r' && array[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
}