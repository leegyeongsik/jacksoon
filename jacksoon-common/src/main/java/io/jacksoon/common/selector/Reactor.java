package io.jacksoon.common.selector;


import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.handler.Handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class Reactor implements Runnable {
    final Selector selector;
    private final ExceptionDispatcher exceptionDispatcher;
    public Reactor(Selector selector, ExceptionDispatcher exceptionDispatcher) {
        this.selector = selector;
        this.exceptionDispatcher = exceptionDispatcher;
    }
    public void register(SelectableChannel channel, Handler handler, int ops) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, ops, handler);
    }
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processOnce();
            } catch (IOException e) {
                dispatchException(e);
                break;
            }
        }
    }
    public void processOnce() throws IOException {
        selector.select();

        Set<SelectionKey> selected = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selected.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (!key.isValid()) {
                continue;
            }
            try {
                dispatch(key);
            } catch (Exception e) {
                dispatchException(key, e);
            }
        }
    }

    void dispatch(SelectionKey selectionKey) {
        Handler handler = (Handler) selectionKey.attachment();
        handler.handle();
    }

    private <C> void dispatchException(C context, Throwable throwable) {
        if (exceptionDispatcher == null) {
            throwable.printStackTrace();
            return;
        }
        exceptionDispatcher.dispatch(context, throwable);
    }

    private void dispatchException(Throwable throwable) {
        if (exceptionDispatcher == null) {
            throwable.printStackTrace();
            return;
        }
        exceptionDispatcher.dispatch(throwable);
    }
}
