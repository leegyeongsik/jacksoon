package io.jacksoon.router;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.init.factory.InitFactory;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.seletor.ReactorQueue;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import io.jacksoon.router.worker.thread.RequestWorkerPool;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Main {
    public static void main(String[] args) throws IOException {
        InitFactory.initialize();
    }
}