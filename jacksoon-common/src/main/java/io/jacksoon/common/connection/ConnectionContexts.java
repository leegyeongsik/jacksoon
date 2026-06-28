package io.jacksoon.common.connection;

import io.jacksoon.common.util.CommonBlockingQueue;

import java.nio.channels.SelectionKey;

public record ConnectionContexts<T>(SelectionKey selectionKey, CommonBlockingQueue<T> requestBackendQueue) {
}
