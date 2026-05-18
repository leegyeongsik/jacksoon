package io.jacksoon.router;

import io.jacksoon.router.handle.AcceptHandler;
import io.jacksoon.router.help.HttpRequestCheck;
import io.jacksoon.router.pipeline.step.Step;
import io.jacksoon.router.seletor.Reactor;
import io.jacksoon.router.worker.thread.RequestPipelineQueue;
import io.jacksoon.router.worker.thread.RequestWorkerPool;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

}