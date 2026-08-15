package io.jacksoon.common.selector;


import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import io.jacksoon.common.handler.Handler;
import io.jacksoon.common.util.CommonBlockingQueue;

public class Reactor implements Runnable{
    final Selector selector;
    boolean isEventReactor = false;
    CommonBlockingQueue<Handler> eventQueue = null;
    EventManagement eventManagement;
    public Reactor(Selector selector){
        this.selector = selector;
    }
    public Reactor(Selector selector , CommonBlockingQueue<Handler> eventQueue,EventManagement eventManagement){
        this.selector = selector;
        isEventReactor = true;
        this.eventQueue = eventQueue;
        this.eventManagement =eventManagement;
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
        Iterator<SelectionKey> iterator = selected.iterator();

        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (!key.isValid()) {
                continue;
            }

            dispatch(key);
        }
    }
    void dispatch(SelectionKey selectionKey) {

        Handler handler = (Handler) selectionKey.attachment();
        if (!isEventReactor) {
            handler.handle();
            return;
        }
        boolean shouldExecute = eventManagement.pendingEvent(handler);
        if (shouldExecute) {
            eventQueue.put(handler);
        }
    }
}
