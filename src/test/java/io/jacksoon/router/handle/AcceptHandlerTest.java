//package io.jacksoon.router.handle;
//
//import io.jacksoon.router.help.HttpRequestCheck;
//import io.jacksoon.router.worker.thread.RequestPipelineQueue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//import java.nio.channels.SocketChannel;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//
//class AcceptHandlerTest {
//    Selector selector;
//    ServerSocketChannel serverSocketChannel;
//    AcceptHandler acceptHandler;
//    RequestPipelineQueue pipelineQueue;
//    HttpRequestCheck httpRequestCheck;
//    @BeforeEach
//    void init() throws IOException {
//        selector = Selector.open();
//        serverSocketChannel = ServerSocketChannel.open();
//        pipelineQueue = mock(RequestPipelineQueue.class);
//        httpRequestCheck = mock(HttpRequestCheck.class);
//        acceptHandler = new AcceptHandler(selector,serverSocketChannel,pipelineQueue,httpRequestCheck);
//    }
//
//    @Test
//    void constructor() throws IOException {
//        assertNotNull(acceptHandler);
//        assertEquals(acceptHandler.selector,selector);
//        assertEquals(acceptHandler.serverSocketChannel,serverSocketChannel);
//        assertEquals(acceptHandler.requestPipelineQueue,pipelineQueue);
//        assertEquals(acceptHandler.httpRequestCheck,httpRequestCheck);
//    }
//    @Test
//    void handle() throws IOException {
//        ServerSocketChannel serverSocketChannel = mock(ServerSocketChannel.class);
//        Selector selector = mock(Selector.class);
//        RequestPipelineQueue queue = mock(RequestPipelineQueue.class);
//        HttpRequestCheck requestCheck = mock(HttpRequestCheck.class);
//        SocketChannel socketChannel = mock(SocketChannel.class);
//        SelectionKey selectionKey = mock(SelectionKey.class);
//        when(serverSocketChannel.accept()).thenReturn(socketChannel);
//        when(socketChannel.register(eq(selector), eq(SelectionKey.OP_READ)))
//                .thenReturn(selectionKey);
//        AcceptHandler acceptHandler =
//                new AcceptHandler(
//                        selector,
//                        serverSocketChannel,
//                        queue,
//                        requestCheck
//                );
//
//        acceptHandler.handle();
//
//        verify(serverSocketChannel).accept();
//
//        verify(socketChannel).configureBlocking(false);
//        verify(socketChannel).register(eq(selector), eq(SelectionKey.OP_READ));
//        verify(selector).wakeup();
//    }
//}