package io.jacksoon.common.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface ConnectionManager {
    default SocketChannel connect(ConnectionContext connectionContext) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(
                    new InetSocketAddress(
                            connectionContext.getHost(),
                            connectionContext.getPort()
                    )
            );
            return socketChannel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void create(SocketChannel socketChannel);
}