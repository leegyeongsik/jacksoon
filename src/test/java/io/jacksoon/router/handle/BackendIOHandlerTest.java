package io.jacksoon.router.handle;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.help.HttpResponseCheck;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.connection.RequestBackendQueue;
import io.jacksoon.router.worker.connection.ResponseBackendQueue;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BackendIOHandlerTest {

    private Selector selector;
    private SocketChannel socketChannel;
    private SelectionKey backendKey;
    private ResponseBackendQueue responseQueue;
    private RequestBackendQueue requestQueue;
    private RequestPipelineQueue pipelineQueue;
    private BufferContext clientBufferContext;
    private SelectionKey clientKey;

    @BeforeEach
    void setUp() throws IOException {
        selector = mock(Selector.class);
        socketChannel = mock(SocketChannel.class);
        backendKey = mock(SelectionKey.class);
        responseQueue = new ResponseBackendQueue();
        requestQueue = new RequestBackendQueue();
        pipelineQueue = mock(RequestPipelineQueue.class);
        clientBufferContext = new BufferContext();
        clientKey = mock(SelectionKey.class);

        when(socketChannel.register(eq(selector), eq(SelectionKey.OP_CONNECT)))
                .thenReturn(backendKey);
    }

    @Test
    void constructorRegistersBackendSocketForConnectAndAttachesHandler() throws ClosedChannelException {
        BackendIOHandler handler = newHandler();

        verify(socketChannel).register(selector, SelectionKey.OP_CONNECT);
        verify(backendKey).attach(handler);
        verify(selector).wakeup();
        assertSame(backendKey, handler.getSelectionKey());
    }

    @Test
    void readCompleteContentLengthResponsePutsBackendResponseContextToPipelineQueue() throws Exception {
        String response =
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 5\r\n" +
                        "\r\n" +
                        "hello";
        responseQueue.put(proxyContext());
        writeToReadBufferWhenSocketRead(response);
        BackendIOHandler handler = newHandler();

        handler.read();

        ArgumentCaptor<PipelineContext> captor = ArgumentCaptor.forClass(PipelineContext.class);
        verify(pipelineQueue).put(captor.capture());

        PipelineContext context = captor.getValue();
        assertSame(socketChannel, context.getSocketChannel());
        assertEquals("backend-response", context.getEvent());
        assertEquals(response.length(), context.getByteBufferIndex());
        assertSame(clientBufferContext, context.getResponse().getBufferContext());
        assertSame(clientKey, context.getResponse().getSelectionKey());
        assertEquals(response, toText(context.getByteBuffer()));
        assertTrue(responseQueue.isEmpty());
        verify(backendKey).interestOps(SelectionKey.OP_READ);
    }

    @Test
    void readIncompleteResponseDoesNotPutContextToPipelineQueue() throws Exception {
        responseQueue.put(proxyContext());
        writeToReadBufferWhenSocketRead(
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 10\r\n" +
                        "\r\n" +
                        "hello"
        );
        BackendIOHandler handler = newHandler();

        handler.read();

        verify(pipelineQueue, never()).put(any(PipelineContext.class));
        assertFalse(responseQueue.isEmpty());
    }

    @Test
    void readEofCompletesCloseDelimitedResponseAndClosesBackend() throws Exception {
        String response =
                "HTTP/1.1 200 OK\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        "hello";
        ProxyContext proxyContext = proxyContext();
        proxyContext.responseBuffer.put(response.getBytes(StandardCharsets.US_ASCII));
        responseQueue.put(proxyContext);
        when(socketChannel.read(any(ByteBuffer.class))).thenReturn(-1);
        BackendIOHandler handler = newHandler();

        handler.read();

        ArgumentCaptor<PipelineContext> captor = ArgumentCaptor.forClass(PipelineContext.class);
        verify(pipelineQueue).put(captor.capture());
        assertEquals(response, toText(captor.getValue().getByteBuffer()));
        assertTrue(responseQueue.isEmpty());
        verify(backendKey).cancel();
        verify(socketChannel).close();
    }

    @Test
    void readReturnsWhenThereIsNoWaitingResponseContext() throws Exception {
        BackendIOHandler handler = newHandler();

        handler.read();

        verify(socketChannel, never()).read(any(ByteBuffer.class));
        verify(pipelineQueue, never()).put(any(PipelineContext.class));
    }

    private BackendIOHandler newHandler() {
        return new BackendIOHandler(
                selector,
                socketChannel,
                responseQueue,
                requestQueue,
                pipelineQueue,
                new HttpResponseCheck()
        );
    }

    private ProxyContext proxyContext() {
        return new ProxyContext(
                ByteBuffer.allocate(0),
                clientBufferContext,
                clientKey
        );
    }

    private void writeToReadBufferWhenSocketRead(String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.US_ASCII);
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
}
