package io.jacksoon.common.handler;

import io.jacksoon.common.util.ResponseContext;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.PriorityQueue;
public class IOStore {
    private final ConcurrentHashMap<SelectionKey, ClientState> clientMap = new ConcurrentHashMap<>();
    void initClient(SelectionKey selectionKey) {
        clientMap.put(selectionKey, new ClientState());
    }
    public void offer(SelectionKey selectionKey, ResponseContext responseContext) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }
        boolean wakeupNeeded = false;
        synchronized (state) {
            if (!selectionKey.isValid()) {
                return;
            }
            state.queue.offer(responseContext);
            discardStaleResponsesLocked(state);
            ResponseContext head = state.queue.peek();
            if (head != null && head.sequence() == state.nextSequence) {
                addInterestOpsLocked(selectionKey, SelectionKey.OP_WRITE);
                wakeupNeeded = true;
            }
        }
        if (wakeupNeeded) {
            selectionKey.selector().wakeup();
        }
    }
    ResponseContext pollReadyOrDisableWrite(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return null;
        }

        synchronized (state) {
            discardStaleResponsesLocked(state);
            ResponseContext head = state.queue.peek();
            if (head == null || head.sequence() != state.nextSequence) {
                removeInterestOpsLocked(selectionKey, SelectionKey.OP_WRITE);
                return null;
            }
            return state.queue.poll();
        }
    }

    void responseCompleted(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }

        synchronized (state) {
            state.nextSequence++;
        }
    }

    void removeClient(SelectionKey selectionKey) {
        clientMap.remove(selectionKey);
    }

    private void discardStaleResponsesLocked(ClientState state) {
        ResponseContext head = state.queue.peek();
        while (head != null && head.sequence() < state.nextSequence) {
            state.queue.poll();
            head = state.queue.peek();
        }
    }

    private void addInterestOpsLocked(SelectionKey selectionKey, int ops) {
        try {
            if (selectionKey.isValid()) {
                selectionKey.interestOps(selectionKey.interestOps() | ops);
            }
        } catch (CancelledKeyException ignored) {
        }
    }

    private void removeInterestOpsLocked(SelectionKey selectionKey, int ops) {
        try {
            if (selectionKey.isValid()) {
                selectionKey.interestOps(selectionKey.interestOps() & ~ops);
            }
        } catch (CancelledKeyException ignored) {
        }
    }

    private static final class ClientState {
        private final PriorityQueue<ResponseContext> queue = new PriorityQueue<>(11, Comparator.comparingInt(ResponseContext::sequence));
        private int nextSequence = 1;
    }
}