package io.jacksoon.common.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
    public ResponseCheckResult eof(ByteBuffer accumulationByteBuffer) {
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
            throw new IllegalStateException("HttpResponseCheck requires a heap ByteBuffer");
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
        HeaderInfo headerInfo = parseHeader(array, arrayStart, headerEnd);
        int statusCode = headerInfo.statusCode;
        if (statusCode >= 100 && statusCode < 200) {
            throw new IllegalStateException("Informational response is not supported yet: " + statusCode);
        }

        boolean connectionClose = headerInfo.connectionClose || (headerInfo.http10 && !headerInfo.connectionKeepAlive);
        if (statusCode == 204 || statusCode == 304) {
            return new ResponseCheckResult(true, headerLength, headerLength, false, connectionClose);
        }

        if (headerInfo.hasTransferEncoding && headerInfo.contentLength >= 0) {
            throw new IllegalStateException("Both Transfer-Encoding and Content-Length are present");
        }
        if (headerInfo.chunked) {
            if (!headerInfo.finalTransferEncodingChunked) {
                throw new IllegalStateException("Chunked must be the final transfer encoding");
            }

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
        if (headerInfo.hasTransferEncoding) {
            if (!eof) {
                return new ResponseCheckResult(false, 0, headerLength, true, true);
            }
            return new ResponseCheckResult(true, totalBytes, headerLength, true, true);
        }
        if (headerInfo.contentLength >= 0) {
            long expectedLength = (long) headerLength + headerInfo.contentLength;
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
    private HeaderInfo parseHeader(byte[] array, int start, int headerEnd) {
        HeaderInfo info = new HeaderInfo();
        int statusLineEnd = findCrlf(array, start, headerEnd + 2);
        if (statusLineEnd == -1) {
            throw new IllegalStateException("Invalid HTTP status line");
        }
        parseStatusLine(array, start, statusLineEnd, info);
        int lineStart = statusLineEnd + 2;
        while (lineStart < headerEnd) {
            int lineEnd = findCrlf(array, lineStart, headerEnd + 2);
            if (lineEnd == -1) {
                throw new IllegalStateException("Invalid HTTP response header");
            }
            if (lineEnd == lineStart) {
                break;
            }
            parseHeaderLine(array, lineStart, lineEnd, info);
            lineStart = lineEnd + 2;
        }
        return info;
    }
    private void parseStatusLine(byte[] array, int start, int end, HeaderInfo info) {
        if (end - start < 8 || !startsWithIgnoreCase(array, start, end, "HTTP/")) {
            throw new IllegalStateException("Invalid HTTP status line: " + asciiString(array, start, end));
        }
        info.http10 = startsWithIgnoreCase(array, start, end, "HTTP/1.0");
        int position = start;
        while (position < end && !isWhitespace(array[position])) {
            position++;
        }
        while (position < end && isWhitespace(array[position])) {
            position++;
        }
        int statusStart = position;
        while (position < end && !isWhitespace(array[position])) {
            position++;
        }
        if (statusStart == position) {
            throw new IllegalStateException("Invalid HTTP status line: " + asciiString(array, start, end));
        }
        int statusCode = parsePositiveDecimal(array, statusStart, position);
        if (statusCode < 100 || statusCode > 999) {
            throw new IllegalStateException("Invalid HTTP status code: " + statusCode);
        }
        info.statusCode = statusCode;
    }
    private void parseHeaderLine(byte[] array, int start, int end, HeaderInfo info) {
        int colon = findByte(array, start, end, (byte) ':');
        if (colon <= start) {
            return;
        }
        int nameStart = trimLeft(array, start, colon);
        int nameEnd = trimRight(array, nameStart, colon);
        int valueStart = trimLeft(array, colon + 1, end);
        int valueEnd = trimRight(array, valueStart, end);
        if (equalsIgnoreCase(array, nameStart, nameEnd, "Connection")) {
            if (containsTokenIgnoreCase(array, valueStart, valueEnd, "close")) {
                info.connectionClose = true;
            }
            if (containsTokenIgnoreCase(array, valueStart, valueEnd, "keep-alive")) {
                info.connectionKeepAlive = true;
            }
            return;
        }
        if (equalsIgnoreCase(array, nameStart, nameEnd, "Transfer-Encoding")) {
            info.hasTransferEncoding = true;
            parseTransferEncoding(array, valueStart, valueEnd, info);
            return;
        }
        if (equalsIgnoreCase(array, nameStart, nameEnd, "Content-Length")) {
            parseContentLength(array, valueStart, valueEnd, info);
        }
    }
    private void parseTransferEncoding(byte[] array, int start, int end, HeaderInfo info) {
        int tokenStart = start;
        while (tokenStart <= end) {
            int comma = findByte(array, tokenStart, end, (byte) ',');
            int tokenEnd = comma == -1 ? end : comma;
            int trimmedStart = trimLeft(array, tokenStart, tokenEnd);
            int trimmedEnd = trimRight(array, trimmedStart, tokenEnd);
            if (trimmedStart < trimmedEnd) {
                boolean isChunked = equalsIgnoreCase(array, trimmedStart, trimmedEnd, "chunked");
                if (isChunked) {
                    info.chunked = true;
                }
                info.finalTransferEncodingChunked = isChunked;
            }
            if (comma == -1) {
                break;
            }
            tokenStart = comma + 1;
        }
    }
    private void parseContentLength(byte[] array, int start, int end, HeaderInfo info) {
        int valueStart = start;
        while (valueStart <= end) {
            int comma = findByte(array, valueStart, end, (byte) ',');
            int valueEnd = comma == -1 ? end : comma;
            int trimmedStart = trimLeft(array, valueStart, valueEnd);
            int trimmedEnd = trimRight(array, trimmedStart, valueEnd);
            if (trimmedStart >= trimmedEnd) {
                throw new IllegalStateException("Invalid Content-Length");
            }
            int current = parsePositiveDecimal(array, trimmedStart, trimmedEnd);
            if (info.contentLength >= 0 && info.contentLength != current) {
                throw new IllegalStateException("Conflicting Content-Length values");
            }
            info.contentLength = current;
            if (comma == -1) {
                break;
            }
            valueStart = comma + 1;
        }
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
        int valueStart = trimLeft(array, start, end);
        int valueEnd = trimRight(array, valueStart, end);
        int semicolon = findByte(array, valueStart, valueEnd, (byte) ';');
        if (semicolon != -1) {
            valueEnd = trimRight(array, valueStart, semicolon);
        }
        if (valueStart >= valueEnd) {
            throw new IllegalStateException("Empty chunk size");
        }
        long result = 0L;
        for (int i = valueStart; i < valueEnd; i++) {
            int digit = hexDigit(array[i]);

            if (digit == -1) {
                throw new IllegalStateException("Invalid chunk size");
            }
            if (result > (Long.MAX_VALUE - digit) / 16) {
                throw new IllegalStateException("Invalid chunk size");
            }
            result = result * 16 + digit;
            if (result > MAX_RESPONSE_SIZE) {
                throw new IllegalStateException("Response too large");
            }
        }
        return result;
    }
    private int parsePositiveDecimal(byte[] array, int start, int end) {
        if (start >= end) {
            throw new IllegalStateException("Invalid decimal value");
        }
        int result = 0;
        for (int i = start; i < end; i++) {
            byte current = array[i];
            if (current < '0' || current > '9') {
                throw new IllegalStateException("Invalid decimal value");
            }
            int digit = current - '0';
            if (result > (Integer.MAX_VALUE - digit) / 10) {
                throw new IllegalStateException("Decimal value too large");
            }
            result = result * 10 + digit;
        }

        return result;
    }
    private boolean containsTokenIgnoreCase(byte[] array, int start, int end, String expected) {
        int tokenStart = start;

        while (tokenStart <= end) {
            int comma = findByte(array, tokenStart, end, (byte) ',');
            int tokenEnd = comma == -1 ? end : comma;
            int trimmedStart = trimLeft(array, tokenStart, tokenEnd);
            int trimmedEnd = trimRight(array, trimmedStart, tokenEnd);

            if (equalsIgnoreCase(array, trimmedStart, trimmedEnd, expected)) {
                return true;
            }

            if (comma == -1) {
                return false;
            }
            tokenStart = comma + 1;
        }
        return false;
    }
    private boolean startsWithIgnoreCase(byte[] array, int start, int end, String expected) {
        if (end - start < expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if (toLowerAscii(array[start + i]) != toLowerAscii((byte) expected.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean equalsIgnoreCase(byte[] array, int start, int end, String expected) {
        int length = end - start;
        if (length != expected.length()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (toLowerAscii(array[start + i]) != toLowerAscii((byte) expected.charAt(i))) {
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
    private boolean isWhitespace(byte value) {
        return value == ' ' || value == '\t';
    }
    private int trimLeft(byte[] array, int start, int end) {
        while (start < end && isWhitespace(array[start])) {
            start++;
        }
        return start;
    }
    private int trimRight(byte[] array, int start, int end) {
        while (end > start && isWhitespace(array[end - 1])) {
            end--;
        }
        return end;
    }
    private int findByte(byte[] array, int start, int end, byte expected) {
        for (int i = start; i < end; i++) {
            if (array[i] == expected) {
                return i;
            }
        }
        return -1;
    }
    private int hexDigit(byte value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return -1;
    }
    private String asciiString(byte[] array, int start, int end) {
        return new String(array, start, end - start, StandardCharsets.US_ASCII);
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
    private static final class HeaderInfo {
        private int statusCode;
        private boolean http10;
        private boolean connectionClose;
        private boolean connectionKeepAlive;
        private boolean hasTransferEncoding;
        private boolean chunked;
        private boolean finalTransferEncodingChunked;
        private int contentLength = -1;
    }
}