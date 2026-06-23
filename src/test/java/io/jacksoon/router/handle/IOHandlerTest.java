//package io.jacksoon.router.handle;
//
//import io.jacksoon.router.help.BufferContext;
//import io.jacksoon.router.help.HttpRequestCheck;
//import io.jacksoon.router.help.RequestCheck;
//import io.jacksoon.router.help.RequestCheckResult;
//import io.jacksoon.router.pipeline.context.PipelineContext;
//import io.jacksoon.router.worker.thread.RequestPipelineQueue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.channels.SocketChannel;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//class IOHandlerTest {
//    SocketChannel socketChannel;
//    RequestCheck requestCheck;
//    BufferContext bufferContext;
//    IOHandler ioHandler;
//    Selector selector;
//    RequestPipelineQueue pipelineQueue;
//    @BeforeEach
//    void init() throws IOException {
//        socketChannel = mock(SocketChannel.class);
//        selector = mock(Selector.class);
//        requestCheck = mock(RequestCheck.class);
//        connectionContext = mock(ConnectionContext.class);
//        pipelineQueue = mock(RequestPipelineQueue.class);
//    }
//
//
//    @Test
//    void constructor() throws IOException {
//        SelectionKey selectionKey = mock(SelectionKey.class);
//        when(socketChannel.register(eq(selector), eq(SelectionKey.OP_READ)))
//                .thenReturn(selectionKey);
//        ioHandler = new IOHandler(pipelineQueue, selector, socketChannel, requestCheck, connectionContext);
//
//        verify(socketChannel).configureBlocking(false);
//        verify(socketChannel).register(eq(selector), eq(SelectionKey.OP_READ));
//        verify(selectionKey).attach(ioHandler);
//        verify(selector).wakeup();
//
//        assertEquals(selectionKey, ioHandler.selectionKey);
//    }
//    @Test
//    void constructorReadHandler() throws IOException {
//        SelectionKey selectionKey = mock(SelectionKey.class);
//        when(socketChannel.register(eq(selector), eq(SelectionKey.OP_READ)))
//                .thenReturn(selectionKey);
//        ioHandler = new IOHandler(new RequestPipelineQueue(), selector, socketChannel, requestCheck, connectionContext);
//        IOHandler spyHandler = spy(ioHandler);
//        spyHandler.state = IOHandler.READING;
//        spyHandler.handle();
//        verify(spyHandler).read();
//        verify(spyHandler, never()).send();
//    }
//    @Test
//    void read() throws IOException {
//        SelectionKey selectionKey = mock(SelectionKey.class);
//        RequestPipelineQueue queue = mock(RequestPipelineQueue.class);
//
//        when(socketChannel.register(selector, SelectionKey.OP_READ))
//                .thenReturn(selectionKey);
//
//        when(socketChannel.read(any(ByteBuffer.class)))
//                .thenReturn(1);
//
//        ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
//        when(connectionContext.getRequestBuffer())
//                .thenReturn(requestBuffer);
//
//        RequestCheckResult result =
//                new RequestCheckResult(true, 100, 50);
//
//        when(requestCheck.check(any(ByteBuffer.class), any(ByteBuffer.class)))
//                .thenReturn(result);
//
//        ioHandler = new IOHandler(
//                queue,
//                selector,
//                socketChannel,
//                requestCheck,
//                connectionContext
//        );
//
//        ioHandler.read();
//
//        verify(queue).put(any(PipelineContext.class));
//        verify(selectionKey).interestOps(SelectionKey.OP_WRITE);
//        assertEquals(IOHandler.SENDING, ioHandler.state);
//
//    }
//}