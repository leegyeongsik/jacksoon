package io.jacksoon.router.worker.connection;

import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.handle.BackendIOHandler;
import io.jacksoon.router.help.HttpResponseCheck;
import io.jacksoon.router.pipeline.executor.router.ConnectionContexts;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Init
public class ConnectionManager{
    private final RequestPipelineQueue pipelineQueue;
    private final Selector selector;
    private final ConnectionRegistry connectionRegistry;
    private final HttpResponseCheck responseCheck;
    public ConnectionManager(RequestPipelineQueue pipelineQueue, @Init("backendSelector")Selector selector, ConnectionRegistry connectionRegistry, HttpResponseCheck responseCheck) {
        this.pipelineQueue = pipelineQueue;
        this.selector = selector;
        this.connectionRegistry = connectionRegistry;
        this.responseCheck = responseCheck;
    }

    public void create(ConnectionContext connectionContext) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(
                    new InetSocketAddress(
                            connectionContext.getHost(),
                            connectionContext.getPort()
                    )
            );

            RequestBackendQueue requestBackendQueue =  new RequestBackendQueue();
            ResponseBackendQueue responseBackendQueue = new ResponseBackendQueue();
            BackendIOHandler backendIOHandler =  new BackendIOHandler(selector,socketChannel,responseBackendQueue,requestBackendQueue,pipelineQueue,responseCheck);
            connectionRegistry.put("a", new ConnectionContexts(backendIOHandler.getSelectionKey(),requestBackendQueue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
