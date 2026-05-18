package io.jacksoon.router.worker.thread;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RequestWorkerPoolTest {
    @Test
    void start() {
        RequestPipelineQueue queue = new RequestPipelineQueue();
        RequestWorkerPool pool = new RequestWorkerPool(queue);
        assertTimeoutPreemptively(Duration.ofSeconds(1), pool::start);
    }

}