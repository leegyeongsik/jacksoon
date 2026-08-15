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
    CommonBlockingQueue<EventWarrap> eventWarrapQueue = null;
    EventManagement eventManagement;
    public Reactor(Selector selector){
        this.selector = selector;
    }
    public Reactor(Selector selector , CommonBlockingQueue<EventWarrap> handlerQueue,EventManagement eventManagement){
        this.selector = selector;
        isEventReactor = true;
        this.eventWarrapQueue = handlerQueue;
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

         if(isEventReactor){
             // 큐에 던짐 그때 레지스트리에서 락잡고 들어가서 offer함
             // offer하고 던지셈 핸들러를
             // 그리고 쌓고
             // 워커가 peek일때 처리
             // peek이 아니면 넘김
             // 그러면 ioh도 리액터 boolean으로 해서 상태 따라감
             // 커넥션 read write 다 끝났을때 poll함
             EventWarrap event = new EventWarrap(handler, ThreadLocalRandom.current().nextLong());
             boolean first = eventManagement.pendingEvent(event);
             if (first) {
                 eventWarrapQueue.put(event);
             }
             // 아니네 이벤트로 쌓으면 안돠네 그러면 순서로 가야되나
         }else {
             handler.handle();
         }

     }
}
