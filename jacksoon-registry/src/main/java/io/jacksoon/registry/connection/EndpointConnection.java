package io.jacksoon.registry.connection;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Getter
public class EndpointConnection {
    private final String key;
    private final String serviceName;
    private final String instanceId;
    private final String host;
    private final int port;
    private final String healthPath;

    @Setter
    private ByteBuffer requestBuffer;

    private final ByteBuffer responseBuffer = ByteBuffer.allocate(4096);

    @Setter
    private boolean connected;

    @Setter
    private int failCount;

    public EndpointConnection(String key, String serviceName, String instanceId, String host, int port, String healthPath) {
        this.key = key;
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.healthPath = healthPath;
    }

    public void prepareHealthCheckRequest() {
        String request = "GET " + healthPath + " HTTP/1.1\r\n" +
                "Host: " + host + ":" + port + "\r\n" +
                "Connection: keep-alive\r\n" +
                "\r\n";

        this.requestBuffer = ByteBuffer.wrap(
                request.getBytes(StandardCharsets.UTF_8)
        );
    }

    public void clearResponseBuffer() {
        responseBuffer.clear();
    }
}