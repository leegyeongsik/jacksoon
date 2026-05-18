package io.jacksoon.router.handle;

import io.jacksoon.router.help.ConnectionContext;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.help.RequestCheckResult;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IOHandlerTest {
    SocketChannel socketChannel;
    RequestCheck requestCheck;
    ConnectionContext connectionContext;
    IOHandler ioHandler;
    Selector selector;
    RequestPipelineQueue pipelineQueue;
    @BeforeEach
    void init() throws IOException {
        socketChannel = mock(SocketChannel.class);
        selector = mock(Selector.class);
        requestCheck = mock(RequestCheck.class);
        connectionContext = mock(ConnectionContext.class);
        pipelineQueue = mock(RequestPipelineQueue.class);
    }
}