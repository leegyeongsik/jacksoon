//package io.jacksoon.router.pipeline.executor.write;
//
//import io.jacksoon.router.help.BufferContext;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//import java.nio.channels.Selector;
//import java.nio.charset.StandardCharsets;
//
//import static org.mockito.Mockito.*;
//
//class HttpWriteTest {
//
//    private final HttpWrite httpWrite = new HttpWrite();
//
//    @Test
//    void storesResponseBufferAndEnablesClientWriteInterest() {
//        BufferContext bufferContext = new BufferContext();
//        ByteBuffer responseBuffer = ByteBuffer.wrap(
//                "HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII)
//        );
//        SelectionKey clientKey = mock(SelectionKey.class);
//        Selector selector = mock(Selector.class);
//
//        when(clientKey.interestOps()).thenReturn(SelectionKey.OP_READ);
//        when(clientKey.selector()).thenReturn(selector);
//
//        PipelineContext context = new PipelineContext(
//                null,
//                "backend-response",
//                responseBuffer,
//                responseBuffer.limit(),
//                bufferContext,
//                clientKey
//        );
//
//        httpWrite.dodo(context);
//
//        Assertions.assertSame(responseBuffer, bufferContext.getResponseBuffer());
//        verify(clientKey).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
//        verify(selector).wakeup();
//    }
//
//    @Test
//    void currentAndNextEventAreCorrect() {
//        Assertions.assertEquals("backend-response", httpWrite.currentEvent());
//        Assertions.assertEquals("", httpWrite.nextEvent());
//    }
//}
