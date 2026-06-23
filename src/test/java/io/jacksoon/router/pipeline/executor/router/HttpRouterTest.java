package io.jacksoon.router.pipeline.executor.router;

import io.jacksoon.router.handle.ProxyContext;
import io.jacksoon.router.help.BufferContext;
import io.jacksoon.router.pipeline.context.PipelineContext;
import io.jacksoon.router.worker.connection.RequestBackendQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpRouterTest {

    @Test
    void putsProxyContextToBackendQueueAndEnablesBackendWriteInterest() throws Exception {
        FindRouter findRouter = mock(FindRouter.class);
        RequestBackendQueue backendQueue = new RequestBackendQueue();
        SelectionKey backendKey = mock(SelectionKey.class);
        Selector backendSelector = mock(Selector.class);
        ConnectionContexts connectionContexts = new ConnectionContexts(backendKey, backendQueue);
        HttpRouter router = new HttpRouter(findRouter);

        ByteBuffer requestBuffer = ByteBuffer.wrap(
                "GET /hello HTTP/1.1\r\n\r\n".getBytes(StandardCharsets.US_ASCII)
        );
        SelectionKey clientKey = mock(SelectionKey.class);
        PipelineContext pipelineContext = new PipelineContext(
                null,
                "router",
                requestBuffer,
                requestBuffer.limit(),
                new BufferContext(),
                clientKey
        );

        when(findRouter.getConnection(any())).thenReturn(connectionContexts);
        when(backendKey.interestOps()).thenReturn(SelectionKey.OP_READ);
        when(backendKey.selector()).thenReturn(backendSelector);

        router.dodo(pipelineContext);

        ProxyContext proxyContext = backendQueue.poll();
        Assertions.assertNotNull(proxyContext);
        Assertions.assertSame(requestBuffer, proxyContext.requestBuffer);
        verify(backendKey).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        verify(backendSelector).wakeup();
    }

    @Test
    void currentAndNextEventAreCorrect() {
        HttpRouter router = new HttpRouter(mock(FindRouter.class));

        Assertions.assertEquals("router", router.currentEvent());
        Assertions.assertEquals("", router.nextEvent());
    }
}
