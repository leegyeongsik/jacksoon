package io.jacksoon.common.selector;

import io.jacksoon.common.handler.Handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
public class EventManagement {
    ConcurrentHashMap<Handler, ConcurrentLinkedQueue<Long>> pendingQueue = new ConcurrentHashMap<>();
    public synchronized boolean pendingEvent(EventWarrap event) {
        ConcurrentLinkedQueue<Long> queue = pendingQueue.computeIfAbsent(event.getHandler(), h -> new ConcurrentLinkedQueue<>());
        boolean first = queue.isEmpty();
        queue.offer(event.getN());
        return first;
    }
    // 여기서 순서대로 쌓자 그리고 peek일때 던진게 peek이면 ㄱㄱ
    // 그리고 하나  끝냈을때 poll다시 큐에 던짐
    public synchronized Long checkPeekPendingEvent(EventWarrap event){
        if(!pendingQueue.containsKey(event.getHandler())){
            throw new IllegalArgumentException();
        }
        return pendingQueue.get(event.getHandler()).peek();
    }

    public synchronized Long completeEvent(Handler handler) {
        ConcurrentLinkedQueue<Long> queue = pendingQueue.get(handler);
        if (queue == null) {
            return null;
        }
        queue.poll();
        Long next = queue.peek();
        if (next == null) {
            pendingQueue.remove(handler);
        }
        return next;
    }
}
