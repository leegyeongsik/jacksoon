package io.jacksoon.router.pipeline.executor.paser;

import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.pipeline.context.RouterRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class HttpParseTest {

    private final HttpParse parser = new HttpParse();

    @Test
    void parsesRequestLineAndHeaders() {
        String request =
                "GET /hello?name=router HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "body";
        ByteBuffer buffer = buffer(request);
        PipelineContext context = context(buffer, headerLength(request));

        parser.dodo(context);

        RouterRequest routerRequest = context.getRequest();
        Assertions.assertEquals("GET", routerRequest.getMethod());
        Assertions.assertEquals("/hello?name=router", routerRequest.getPath());
        Assertions.assertEquals("HTTP/1.1", routerRequest.getVersion());
        Assertions.assertEquals("localhost", routerRequest.getHeaders().get("Host"));
        Assertions.assertEquals("text/plain", routerRequest.getHeaders().get("Content-Type"));
    }

    @Test
    void keepsOriginalBufferPositionAndLimit() {
        String request =
                "POST /users HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Length: 5\r\n" +
                        "\r\n" +
                        "hello";
        ByteBuffer buffer = buffer(request);
        int originalPosition = buffer.position();
        int originalLimit = buffer.limit();
        PipelineContext context = context(buffer, headerLength(request));

        parser.dodo(context);

        Assertions.assertEquals(originalPosition, buffer.position());
        Assertions.assertEquals(originalLimit, buffer.limit());
    }

    @Test
    void trimsHeaderNameAndValue() {
        String request =
                "GET / HTTP/1.1\r\n" +
                        "Host:   localhost   \r\n" +
                        "X-Test :  value  \r\n" +
                        "\r\n";
        PipelineContext context = context(buffer(request), headerLength(request));

        parser.dodo(context);

        Assertions.assertEquals("localhost", context.getRequest().getHeaders().get("Host"));
        Assertions.assertEquals("value", context.getRequest().getHeaders().get("X-Test"));
    }

    @Test
    void ignoresMalformedHeaderLine() {
        String request =
                "GET / HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "wrong-header-line\r\n" +
                        "\r\n";
        PipelineContext context = context(buffer(request), headerLength(request));

        parser.dodo(context);

        Assertions.assertEquals("localhost", context.getRequest().getHeaders().get("Host"));
        Assertions.assertFalse(context.getRequest().getHeaders().containsKey("wrong-header-line"));
    }

    @Test
    void throwsExceptionWhenRequestLineIsInvalid() {
        String request = "GET /hello\r\n\r\n";
        PipelineContext context = context(buffer(request), headerLength(request));

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> parser.dodo(context)
        );
    }

    @Test
    void currentAndNextEventAreCorrect() {
        Assertions.assertEquals("parse", parser.currentEvent());
        Assertions.assertEquals("router", parser.nextEvent());
    }

    private PipelineContext context(ByteBuffer buffer, int headerLength) {
        return new PipelineContext(
                null,
                "parse",
                buffer,
                headerLength,
                new BufferContext(),
                null
        );
    }

    private ByteBuffer buffer(String text) {
        return ByteBuffer.wrap(text.getBytes(StandardCharsets.US_ASCII));
    }

    private int headerLength(String httpMessage) {
        return httpMessage.indexOf("\r\n\r\n") + 4;
    }
}
