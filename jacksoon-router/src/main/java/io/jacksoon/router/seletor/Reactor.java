package io.jacksoon.router.seletor;

import io.jacksoon.router.handle.Handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;
public class Reactor implements Runnable{
    final Selector selector;
    public Reactor(Selector selector) throws IOException {
        this.selector = selector;
    }
    public void register(SelectableChannel channel, Handler handler ,int ops) throws IOException {
        channel.configureBlocking(false);
        SelectionKey key = channel.register(selector, ops);
        key.attach(handler);
    }
    @Override
    public void run() {
        try {
            while (true) {
                processOnce();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public void processOnce() throws IOException {
        selector.select();
        Set<SelectionKey> selected = selector.selectedKeys();
        for (SelectionKey key : selected) {
            dispatch(key);
        }
        selected.clear();
    }
     void dispatch(SelectionKey selectionKey) {
        Handler handler = (Handler) selectionKey.attachment();
        handler.handle();
    }
}
