package io.jacksoon.common.handler;

import java.nio.channels.SelectionKey;

public interface ClientConnectionLifecycle {

    ClientConnectionLifecycle NO_OP = new ClientConnectionLifecycle() {
    };

    default void connected(SelectionKey selectionKey, Runnable closeAction) {
    }

    default void readActivity(SelectionKey selectionKey) {
    }

    default boolean requestSubmitted(SelectionKey selectionKey) {
        return true;
    }

    default void requestFailed(SelectionKey selectionKey) {
    }

    default void responseCompleted(SelectionKey selectionKey) {
    }

    default void closed(SelectionKey selectionKey) {
    }
}