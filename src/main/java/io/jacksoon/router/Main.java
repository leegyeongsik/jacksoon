package io.jacksoon.router;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.handle.worker.CheckQueue;
import io.jacksoon.router.handle.worker.CheckWorkerPool;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.help.RequestCheck;
import io.jacksoon.router.pipeline.step.Step;
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
        CheckQueue checkQueue = new CheckQueue();
        Step step = new Step();
        RequestCheck requestCheck = new HttpRequestCheck();
        RequestPipelineQueue queue = new RequestPipelineQueue();

        AcceptHandler acceptHandler =
                new AcceptHandler(selector,serverSocketChannel ,new HttpRequestCheck());

        Reactor reactor =
                new Reactor(selector, serverSocketChannel,1012, acceptHandler);
        new Thread(reactor).start();
        new CheckWorkerPool(checkQueue,queue,requestCheck,step).start();
        new RequestWorkerPool(queue).start();

    }
}