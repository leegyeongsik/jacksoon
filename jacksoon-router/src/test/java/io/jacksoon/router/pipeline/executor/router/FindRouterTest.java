//package io.jacksoon.router.pipeline.executor.router;
//
//import io.jacksoon.router.help.BufferContext;
//import io.jacksoon.router.pipeline.context.RouterRequest;
//import io.jacksoon.router.worker.connection.ConnectionRegistry;
//import io.jacksoon.router.worker.connection.RequestBackendQueue;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.SelectionKey;
//
//import static org.mockito.Mockito.mock;
//
//class FindRouterTest {
//
//    @Test
//    void returnsRegisteredConnectionContext() {
//        ConnectionRegistry registry = new ConnectionRegistry();
//        io.jacksoon.common.worker.connection.ConnectionContexts expected = new io.jacksoon.common.worker.connection.ConnectionContexts(
//                mock(SelectionKey.class),
//                new RequestBackendQueue()
//        );
//        registry.put("a", expected);
//        FindRouter findRouter = new FindRouter(registry);
//        RouterRequest request = newRequest("/users");
//
//        io.jacksoon.common.worker.connection.ConnectionContexts result = findRouter.getConnection(request);
//
//        Assertions.assertSame(expected, result);
//    }
//
//    @Test
//    void throwsExceptionWhenConnectionDoesNotExist() {
//        FindRouter findRouter = new FindRouter(new ConnectionRegistry());
//        RouterRequest request = newRequest("/users");
//
//        Assertions.assertThrows(
//                RuntimeException.class,
//                () -> findRouter.getConnection(request)
//        );
//    }
//
//    private RouterRequest newRequest(String path) {
//        PipelineContext context = new PipelineContext(
//                null,
//                "router",
//                ByteBuffer.allocate(0),
//                0,
//                new BufferContext(),
//                null
//        );
//        context.getRequest().setPath(path);
//        return context.getRequest();
//    }
//}
