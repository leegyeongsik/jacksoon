package io.jacksoon.router.handle;

import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AcceptHandlerTest {
    Selector selector;
    ServerSocketChannel serverSocketChannel;
    AcceptHandler acceptHandler;
    RequestPipelineQueue pipelineQueue;
    HttpRequestCheck httpRequestCheck;

}