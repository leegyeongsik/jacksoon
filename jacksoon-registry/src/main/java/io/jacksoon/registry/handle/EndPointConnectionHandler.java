package io.jacksoon.registry.handle;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.EndpointConnection;
import io.jacksoon.registry.connection.event.EndPointConnectionEvent;
import io.jacksoon.registry.connection.event.EndPointEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EndPointConnectionHandler extends NioConnectionHandler {

    private static final long HEALTH_CHECK_TIMEOUT_MILLIS = 5_000L;

    private final EndpointConnection connection;
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry;
    private final CommonBlockingQueue<EndPointEvent> endpointEventQueue;
    // 여기서 락을 쥐고 들어가야하나  들어가는게 핸들러 생성 스레드 , 헬스체크 워커스레드 , 리액터 스레든데 만약에 락을쥐고 들어가면 워커만 락쥐고있으면 버리고 다음에 요청하는게 맞음
    private final AtomicBoolean healthCheckInProgress = new AtomicBoolean(false);
    private final AtomicBoolean successEventPublished = new AtomicBoolean(false);
    private final AtomicBoolean terminated = new AtomicBoolean(false);
    private volatile long healthCheckStartedAt;
    public EndPointConnectionHandler(Selector selector, SocketChannel socketChannel, EndpointConnection connection, ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry, CommonBlockingQueue<EndPointEvent> endpointEventQueue) {
        super(selector, socketChannel, SelectionKey.OP_CONNECT);
        this.connection = connection;
        this.endpointConnectionRegistry = endpointConnectionRegistry;
        this.endpointEventQueue = endpointEventQueue;
    }
    @Override
    public void handle() {
        try {
            if (!selectionKey.isValid()) {
                return;
            }

            if (selectionKey.isConnectable()) {
                connect();
                return;
            }

            if (selectionKey.isWritable()) {
                writeHealthCheck();
                return;
            }

            if (selectionKey.isReadable()) {
                readHealthCheckResponse();
            }
        } catch (CancelledKeyException e) {
            if (!terminated.get()) {
                fail("selection key cancelled: " + e.getMessage());
            }
        } catch (IOException | RuntimeException e) {
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }
        connection.setConnected(true);
        setInterestOps(0);
        endpointEventQueue.put(new EndPointConnectionEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "connection", this));
    }

    private void writeHealthCheck() throws IOException {
        if (!healthCheckInProgress.get()) {
            setInterestOps(0);
            return;
        }
        ByteBuffer requestBuffer = connection.getRequestBuffer();
        if (requestBuffer == null) {
            fail("health check request buffer is null");
            return;
        }
        socketChannel.write(requestBuffer);
        if (requestBuffer.hasRemaining()) {
            setInterestOps(SelectionKey.OP_WRITE);
            return;
        }
        connection.clearResponseBuffer();
        setInterestOps(SelectionKey.OP_READ);
    }
    private void readHealthCheckResponse() throws IOException {
        ByteBuffer responseBuffer = connection.getResponseBuffer();
        if (responseBuffer == null) {
            fail("health check response buffer is null");
            return;
        }
        if (!responseBuffer.hasRemaining()) {
            fail("health check response exceeded buffer capacity: " + responseBuffer.capacity());
            return;
        }
        int read = socketChannel.read(responseBuffer);
        if (read == -1) {
            fail("endpoint closed connection");
            return;
        }
        if (read == 0) {
            return;
        }
        if (!isHttpResponseComplete(responseBuffer)) {
            setInterestOps(SelectionKey.OP_READ);
            return;
        }
        String response = new String(responseBuffer.array(), 0, responseBuffer.position(), StandardCharsets.US_ASCII);
        int statusCode = parseStatusCode(response);
        if (statusCode != 200) {
            fail("unhealthy response" + ", statusLine=" + firstLine(response) + ", statusCode=" + statusCode + ", connection=" + findHeader(response, "Connection"));
            return;
        }
        completeHealthCheckSuccess();
    }
    private void completeHealthCheckSuccess() {
        setInterestOps(0);
        healthCheckStartedAt = 0L;
        healthCheckInProgress.set(false);
        if (successEventPublished.compareAndSet(false, true)) {
            endpointEventQueue.put(new EndPointEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "success"));
        }
    }
    public void fireHealthCheckEvent() {
        if (terminated.get()
                || !connection.isConnected()
                || !socketChannel.isOpen()
                || !socketChannel.isConnected()
                || !selectionKey.isValid()) {
            return;
        }
        if (!healthCheckInProgress.compareAndSet(false, true)) {
            checkHealthCheckTimeout();
            return;
        }
        healthCheckStartedAt = System.currentTimeMillis();
        try {
            connection.prepareHealthCheckRequest();
            connection.clearResponseBuffer();
            if (connection.getRequestBuffer() == null) {
                fail("request buffer is null after preparation");
                return;
            }
            setInterestOps(SelectionKey.OP_WRITE);
        } catch (RuntimeException e) {
            fail("failed to prepare health check: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    private void checkHealthCheckTimeout() {
        long startedAt = healthCheckStartedAt;
        if (startedAt <= 0L) {
            return;
        }
        long elapsed = System.currentTimeMillis() - startedAt;
        if (elapsed >= HEALTH_CHECK_TIMEOUT_MILLIS) {
            fail("health check timeout: elapsed=" + elapsed + "ms");
        }
    }
    private boolean isHttpResponseComplete(ByteBuffer responseBuffer) {
        byte[] bytes = responseBuffer.array();
        int totalBytes = responseBuffer.position();
        int headerEnd = findHeaderEnd(bytes, totalBytes);
        if (headerEnd == -1) {
            return false;
        }
        int bodyStart = headerEnd + 4;
        String headers = new String(bytes, 0, bodyStart, StandardCharsets.US_ASCII);
        int statusCode = parseStatusCode(headers);
        if ((statusCode >= 100 && statusCode < 200)
                || statusCode == 204
                || statusCode == 304) {
            return true;
        }
        String transferEncoding = findHeader(headers, "Transfer-Encoding");
        if (containsToken(transferEncoding, "chunked")) {
            return isChunkedBodyComplete(bytes, bodyStart, totalBytes);
        }
        int contentLength = parseContentLength(headers);
        if (contentLength >= 0) {
            return totalBytes - bodyStart >= contentLength;
        }
        return true;
    }
    private boolean isChunkedBodyComplete(byte[] bytes, int bodyStart, int totalBytes) {
        int cursor = bodyStart;
        while (true) {
            int sizeLineEnd = findCrlf(bytes, cursor, totalBytes);
            if (sizeLineEnd == -1) {
                return false;
            }
            String sizeLine = new String(bytes, cursor, sizeLineEnd - cursor, StandardCharsets.US_ASCII).trim();
            int extensionIndex = sizeLine.indexOf(';');
            if (extensionIndex >= 0) {
                sizeLine = sizeLine.substring(0, extensionIndex).trim();
            }
            if (sizeLine.isEmpty()) {
                throw new IllegalStateException("Invalid empty chunk-size line");
            }
            final long chunkSize;
            try {
                chunkSize = Long.parseLong(sizeLine, 16);
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "Invalid chunk size: " + sizeLine,
                        e
                );
            }
            if (chunkSize < 0 || chunkSize > Integer.MAX_VALUE) {
                throw new IllegalStateException(
                        "Unsupported chunk size: " + chunkSize
                );
            }
            int chunkDataStart = sizeLineEnd + 2;
            if (chunkSize == 0) {
                return areChunkTrailersComplete(bytes, chunkDataStart, totalBytes);
            }
            long chunkDataEndLong = (long) chunkDataStart + chunkSize;
            if (chunkDataEndLong + 2 > totalBytes) {
                return false;
            }
            int chunkDataEnd = (int) chunkDataEndLong;
            if (bytes[chunkDataEnd] != '\r' || bytes[chunkDataEnd + 1] != '\n') {
                throw new IllegalStateException("Invalid chunk terminator");
            }
            cursor = chunkDataEnd + 2;
        }
    }
    private boolean areChunkTrailersComplete(byte[] bytes, int trailerStart, int totalBytes) {
        int cursor = trailerStart;
        while (true) {
            int lineEnd = findCrlf(bytes, cursor, totalBytes);
            if (lineEnd == -1) {
                return false;
            }
            if (lineEnd == cursor) {
                return true;
            }
            cursor = lineEnd + 2;
        }
    }
    private int parseContentLength(String headers) {
        String value = findHeader(headers, "Content-Length");
        if ("<none>".equals(value)) {
            return -1;
        }
        try {
            int contentLength = Integer.parseInt(value);
            if (contentLength < 0) {
                throw new IllegalStateException(
                        "Negative Content-Length: " + value
                );
            }
            return contentLength;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid Content-Length: " + value, e);
        }
    }

    private int findHeaderEnd(byte[] bytes, int totalBytes) {
        for (int i = 0; i <= totalBytes - 4; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n' && bytes[i + 2] == '\r' && bytes[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }
    private int findCrlf(byte[] bytes, int start, int totalBytes) {
        for (int i = start; i <= totalBytes - 2; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n') {
                return i;
            }
        }
        return -1;
    }
    private boolean containsToken(String headerValue, String token) {
        if (headerValue == null || "<none>".equals(headerValue)) {
            return false;
        }

        for (String value : headerValue.split(",")) {
            if (value.trim().equalsIgnoreCase(token)) {
                return true;
            }
        }

        return false;
    }

    private int parseStatusCode(String response) {
        if (response == null || response.isBlank()) {
            return -1;
        }
        int lineEnd = response.indexOf("\r\n");
        if (lineEnd == -1) {
            return -1;
        }
        String statusLine = response.substring(0, lineEnd).trim();
        String[] parts = statusLine.split("\\s+", 3);
        if (parts.length < 2) {
            return -1;
        }
        String httpVersion = parts[0];
        if (!httpVersion.equals("HTTP/1.1")
                && !httpVersion.equals("HTTP/1.0")) {
            return -1;
        }
        try {
            int statusCode = Integer.parseInt(parts[1]);
            if (statusCode < 100 || statusCode > 599) {
                return -1;
            }
            return statusCode;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String firstLine(String response) {
        if (response == null || response.isBlank()) {
            return "<empty>";
        }
        int lineEnd = response.indexOf("\r\n");
        if (lineEnd == -1) {
            return response;
        }
        return response.substring(0, lineEnd);
    }
    private String findHeader(String response, String headerName) {
        if (response == null || response.isBlank()) {
            return "<none>";
        }
        int headerEnd = response.indexOf("\r\n\r\n");
        String headerSection = headerEnd == -1 ? response : response.substring(0, headerEnd);
        String expectedHeader = headerName.toLowerCase(Locale.ROOT) + ":";
        for (String line : headerSection.split("\r\n")) {
            String lowerLine = line.toLowerCase(Locale.ROOT);
            if (!lowerLine.startsWith(expectedHeader)) {
                continue;
            }
            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                return "<invalid>";
            }
            return line.substring(colonIndex + 1).trim();
        }
        return "<none>";
    }

    private void fail(String reason) {
        if (!terminated.compareAndSet(false, true)) {
            return;
        }
        healthCheckStartedAt = 0L;
        healthCheckInProgress.set(false);
        connection.setConnected(false);
        close();
        endpointConnectionRegistry.remove(connection.getKey());
        endpointEventQueue.put(new EndPointEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "fail")
        );
    }
}