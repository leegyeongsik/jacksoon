package io.jacksoon.router;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import io.jacksoon.router.worker.thread.RequestWorkerPool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Main {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        RequestPipelineQueue queue = new RequestPipelineQueue();

        AcceptHandler acceptHandler =
                new AcceptHandler(selector,serverSocketChannel , queue,new HttpRequestCheck());

        Reactor reactor =
                new Reactor(selector, serverSocketChannel,1012, acceptHandler);
        new Thread(reactor).start();
        new RequestWorkerPool(queue).start();

    }
}