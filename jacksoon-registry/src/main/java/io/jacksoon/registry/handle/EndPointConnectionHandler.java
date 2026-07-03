package io.jacksoon.registry.handle;

import io.jacksoon.common.connection.ConnectionHandlerRegistry;
import io.jacksoon.common.handler.NioConnectionHandler;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.registry.connection.EndpointConnection;
import io.jacksoon.registry.connection.event.EndPointEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class EndPointConnectionHandler extends NioConnectionHandler {
    private final EndpointConnection connection;
    private final ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry;
    private final CommonBlockingQueue<EndPointEvent> endpointEventQueue;
    public EndPointConnectionHandler(Selector selector, SocketChannel socketChannel, EndpointConnection connection, ConnectionHandlerRegistry<EndPointConnectionHandler> endpointConnectionRegistry, CommonBlockingQueue<EndPointEvent> endpointEventQueue) {
        super(selector, socketChannel, initInterestOps(socketChannel, connection));
        this.connection = connection;
        this.endpointConnectionRegistry = endpointConnectionRegistry;
        this.endpointEventQueue = endpointEventQueue;
    }

    private static int initInterestOps(SocketChannel socketChannel, EndpointConnection connection) {
        if (socketChannel.isConnected()) {
            connection.setConnected(true);
            connection.setFailCount(0);
            connection.prepareHealthCheckRequest();
            return SelectionKey.OP_WRITE;
        }
        return SelectionKey.OP_CONNECT;
    }

    @Override
    public void handle() {
        try {
            if (!selectionKey.isValid()) {
                fail("selection key is invalid");
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
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private void connect() throws IOException {
        if (!socketChannel.finishConnect()) {
            return;
        }

        connection.setConnected(true);
        connection.setFailCount(0);
        connection.prepareHealthCheckRequest();
        endpointEventQueue.put(new EndPointEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "success"));
        endpointEventQueue.put(new EndPointEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "connection"));
        setInterestOps(SelectionKey.OP_WRITE);
    }

    private void writeHealthCheck() throws IOException {
        ByteBuffer requestBuffer = connection.getRequestBuffer();

        if (requestBuffer == null) {
            connection.prepareHealthCheckRequest();
            requestBuffer = connection.getRequestBuffer();
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

        int read = socketChannel.read(responseBuffer);

        if (read == -1) {
            fail("endpoint closed connection");
            return;
        }

        if (read == 0) {
            return;
        }

        if (!isHttpHeaderComplete(responseBuffer)) {
            setInterestOps(SelectionKey.OP_READ);
            return;
        }

        String response = new String(responseBuffer.array(), 0, responseBuffer.position(), StandardCharsets.UTF_8);

        if (isHealthy(response)) {
            connection.setFailCount(0);
            setInterestOps(0);
            return;
        }

        fail("unhealthy response");
    }

    public void fireHealthCheckEvent() {
        if (!connection.isConnected()) {
            return;
        }

        if (!selectionKey.isValid()) {
            return;
        }

        connection.prepareHealthCheckRequest();
        connection.clearResponseBuffer();

        addInterestOps(SelectionKey.OP_WRITE);
    }

    private boolean isHttpHeaderComplete(ByteBuffer responseBuffer) {
        String response = new String(responseBuffer.array(), 0, responseBuffer.position(), StandardCharsets.UTF_8);

        return response.contains("\r\n\r\n");
    }

    private boolean isHealthy(String response) {
        return response.startsWith("HTTP/1.1 200") || response.startsWith("HTTP/1.0 200");
    }

    private void fail(String reason) {
        connection.setConnected(false);
        close();
        endpointConnectionRegistry.remove(connection.getKey());
        // 이거 뭐 어떻게든 커넥션이 안맺어지면 그냥 그대로 fail이고  최초 맺어진 이후에는 연결되고 그 다음에 fail일어날테니까 상관없을거같은데 정 그러면 큐 하나 두던가 순서대로 넣고 순서대로 빠지도록
        endpointEventQueue.put(new EndPointEvent(connection.getKey(), connection.getServiceName(), connection.getInstanceId(), "fail"));
    }
}