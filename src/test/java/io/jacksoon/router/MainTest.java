//package io.jacksoon.router;
//
//import io.jacksoon.router.handle.AcceptHandler;
//import io.jacksoon.router.help.HttpRequestCheck;
//import io.jacksoon.router.pipeline.step.Step;
//import io.jacksoon.router.seletor.Reactor;
//import io.jacksoon.router.worker.thread.RequestPipelineQueue;
//import io.jacksoon.router.worker.thread.RequestWorkerPool;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.nio.channels.Selector;
//import java.nio.channels.ServerSocketChannel;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class MainTest {
//    @Test
//    void mainComponents_shouldBeCreatedSuccessfully() throws IOException {
//        Selector selector = Selector.open();
//        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
//
//        RequestPipelineQueue queue = new RequestPipelineQueue();
//
//        AcceptHandler acceptHandler =
//                new AcceptHandler(
//                        selector,
//                        serverSocketChannel,
//                        queue,
//                        new HttpRequestCheck()
//                );
//
//        Reactor reactor =
//                new Reactor(
//                        selector,
//                        serverSocketChannel,
//                        1012,
//                        acceptHandler
//                );
//
////        Step step = new Step();
//        RequestWorkerPool workerPool = new RequestWorkerPool(queue);
//
//        assertNotNull(selector);
//        assertTrue(selector.isOpen());
//
//        assertNotNull(serverSocketChannel);
//        assertTrue(serverSocketChannel.isOpen());
//
//        assertNotNull(queue);
//        assertNotNull(acceptHandler);
//        assertNotNull(reactor);
////        assertNotNull(step);
//        assertNotNull(workerPool);
//
//        serverSocketChannel.close();
//        selector.close();
//    }
//
//}