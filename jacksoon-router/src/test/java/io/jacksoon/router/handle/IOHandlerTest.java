package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IOHandlerTest {

    private Selector selector;
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private RequestPipelineQueue pipelineQueue;
    private BufferContext bufferContext;

    @BeforeEach
    void setUp() throws IOException {
        selector = mock(Selector.class);
        socketChannel = mock(SocketChannel.class);
        selectionKey = mock(SelectionKey.class);
        pipelineQueue = mock(RequestPipelineQueue.class);
        bufferContext = new BufferContext();

        when(socketChannel.register(eq(selector), eq(SelectionKey.OP_READ)))
                .thenReturn(selectionKey);
    }

    @Test
    void constructorRegistersSocketForReadAndAttachesHandler() throws IOException {
        IOHandler handler = newHandler();

        verify(socketChannel).configureBlocking(false);
        verify(socketChannel).register(selector, SelectionKey.OP_READ);
        verify(selectionKey).attach(handler);
        verify(selector).wakeup();
        assertSame(selectionKey, handler.selectionKey);
    }

    @Test
    void readCompleteRequestPutsParseContextToPipelineQueue() throws IOException {
        String request =
                "GET /hello HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "\r\n";
        writeToReadBufferWhenSocketRead(request);
        IOHandler handler = newHandler();

        boolean alive = handler.read();

        assertTrue(alive);
        ArgumentCaptor<PipelineContext> captor = ArgumentCaptor.forClass(PipelineContext.class);
        verify(pipelineQueue).put(captor.capture());

        PipelineContext context = captor.getValue();
        assertSame(socketChannel, context.getSocketChannel());
        assertEquals("parse", context.getEvent());
        assertEquals(headerLength(request), context.getByteBufferIndex());
        assertSame(bufferContext, context.getResponse().getBufferContext());
        assertSame(selectionKey, context.getResponse().getSelectionKey());
        assertEquals(request, toText(context.getByteBuffer()));
    }

    @Test
    void readIncompleteRequestDoesNotPutContextToPipelineQueue() throws IOException {
        writeToReadBufferWhenSocketRead(
                "GET /hello HTTP/1.1\r\n" +
                        "Host: localhost\r\n"
        );
        IOHandler handler = newHandler();

        boolean alive = handler.read();

        assertTrue(alive);
        verify(pipelineQueue, never()).put(any(PipelineContext.class));
    }

    @Test
    void readZeroKeepsConnectionAliveAndDoesNothing() throws IOException {
        when(socketChannel.read(any(ByteBuffer.class))).thenReturn(0);
        IOHandler handler = newHandler();

        boolean alive = handler.read();

        assertTrue(alive);
        verify(pipelineQueue, never()).put(any(PipelineContext.class));
        verify(selectionKey, never()).cancel();
        verify(socketChannel, never()).close();
    }

    @Test
    void readEofClosesClientConnection() throws IOException {
        when(socketChannel.read(any(ByteBuffer.class))).thenReturn(-1);
        IOHandler handler = newHandler();

        boolean alive = handler.read();

        assertFalse(alive);
        verify(selectionKey).cancel();
        verify(socketChannel).close();
    }

    @Test
    void sendKeepsWriteInterestWhenResponseBufferStillHasRemaining() throws IOException {
        IOHandler handler = newHandler();
        ByteBuffer response = ByteBuffer.wrap("hello".getBytes(StandardCharsets.US_ASCII));
        bufferContext.setResponseBuffer(response);

        when(selectionKey.interestOps()).thenReturn(SelectionKey.OP_READ);
        when(socketChannel.write(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer buffer = invocation.getArgument(0);
            buffer.position(buffer.position() + 2);
            return 2;
        });

        handler.send();

        assertTrue(response.hasRemaining());
        verify(selectionKey).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    @Test
    void sendSwitchesBackToReadWhenResponseBufferIsFullyWritten() throws IOException {
        IOHandler handler = newHandler();
        ByteBuffer response = ByteBuffer.wrap("hello".getBytes(StandardCharsets.US_ASCII));
        bufferContext.setResponseBuffer(response);

        when(socketChannel.write(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer buffer = invocation.getArgument(0);
            int remaining = buffer.remaining();
            buffer.position(buffer.limit());
            return remaining;
        });

        handler.send();

        assertEquals(0, bufferContext.getResponseBuffer().capacity());
        verify(selectionKey).interestOps(SelectionKey.OP_READ);
    }

    private IOHandler newHandler() throws IOException {
        return new IOHandler(
                pipelineQueue,
                selector,
                socketChannel,
                new HttpRequestCheck(),
                bufferContext
        );
    }

    private void writeToReadBufferWhenSocketRead(String request) throws IOException {
        byte[] bytes = request.getBytes(StandardCharsets.US_ASCII);
        when(socketChannel.read(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer destination = invocation.getArgument(0);
            destination.put(bytes);
            return bytes.length;
        });
    }

    private String toText(ByteBuffer buffer) {
        ByteBuffer duplicate = buffer.duplicate();
        byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private int headerLength(String httpMessage) {
        return httpMessage.indexOf("\r\n\r\n") + 4;
    }
}
