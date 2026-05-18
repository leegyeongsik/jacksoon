package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.context.RouterRequest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpParseTest {
    @Test
    void parse_HTTP() {
        String request =
                "GET /hello HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "body";

        byte[] bytes = request.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(bytes);
        buffer.flip();

        RouterRequest routerRequest = mock(RouterRequest.class);
        when(routerRequest.getHeaders()).thenReturn(new HashMap<>());

        PipelineContext context = mock(PipelineContext.class);
        when(context.getRequest()).thenReturn(routerRequest);
        when(context.getByteBuffer()).thenReturn(buffer);
        when(context.getByteBufferIndex()).thenReturn(
                request.indexOf("\r\n\r\n") + 4
        );

        HttpParse parser = new HttpParse();

        parser.parse(context);

        verify(routerRequest).setMethod("GET");
        verify(routerRequest).setPath("/hello");
        verify(routerRequest).setVersion("HTTP/1.1");

        assertEquals("localhost",
                routerRequest.getHeaders().get("Host"));
        assertEquals("text/plain",
                routerRequest.getHeaders().get("Content-Type"));

        verify(context).setByteBufferIndex(4);

        assertEquals(0, buffer.position());
        assertEquals(4, buffer.limit());
    }

    @Test
    void parse_throw() {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        RouterRequest routerRequest = mock(RouterRequest.class);
        when(routerRequest.getHeaders()).thenReturn(new HashMap<>());

        PipelineContext context = mock(PipelineContext.class);
        when(context.getRequest()).thenReturn(routerRequest);
        when(context.getByteBuffer()).thenReturn(buffer);
        when(context.getByteBufferIndex()).thenReturn(0);

        HttpParse parser = new HttpParse();

        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(context));
    }

    @Test
    void parse_wrong_throw() {
        String request = "GET /hello\r\n\r\n";

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(request.getBytes());
        buffer.flip();

        RouterRequest routerRequest = mock(RouterRequest.class);
        when(routerRequest.getHeaders()).thenReturn(new HashMap<>());

        PipelineContext context = mock(PipelineContext.class);
        when(context.getRequest()).thenReturn(routerRequest);
        when(context.getByteBuffer()).thenReturn(buffer);
        when(context.getByteBufferIndex()).thenReturn(
                request.indexOf("\r\n\r\n") + 4
        );

        HttpParse parser = new HttpParse();

        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(context));
    }

}