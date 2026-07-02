package io.jacksoon.common.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public interface ConnectionManager<T extends ConnectionContext> {

    default SocketChannel connect(T context) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(
                    new InetSocketAddress(
                            context.getHost(),
                            context.getPort()
                    )
            );
            return socketChannel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void create(T context, SocketChannel socketChannel);

    default void connectAndCreate(T context) {
        SocketChannel socketChannel = connect(context);
        create(context, socketChannel);
    }
}