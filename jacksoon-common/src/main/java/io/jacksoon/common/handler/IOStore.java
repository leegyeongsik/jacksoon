package io.jacksoon.common.handler;

import io.jacksoon.common.util.ResponseContext;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
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
        synchronized (state) {
            if (!selectionKey.isValid()) {
                return;
            }
            state.queue.offer(responseContext);
            ResponseContext head = state.queue.peek();
            if (head != null && head.sequence() == state.nextSequence) {
                addInterestOpsLocked(selectionKey, SelectionKey.OP_WRITE);
            }
        }
        selectionKey.selector().wakeup();
    }
    ResponseContext pollReadyOrDisableWrite(SelectionKey selectionKey) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return null;
        }

        synchronized (state) {
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

    void addInterestOps(SelectionKey selectionKey, int ops) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }

        synchronized (state) {
            addInterestOpsLocked(selectionKey, ops);
        }
        selectionKey.selector().wakeup();
    }

    void removeInterestOps(SelectionKey selectionKey, int ops) {
        ClientState state = clientMap.get(selectionKey);
        if (state == null) {
            return;
        }

        synchronized (state) {
            removeInterestOpsLocked(selectionKey, ops);
        }
        selectionKey.selector().wakeup();
    }

    void removeClient(SelectionKey selectionKey) {
        clientMap.remove(selectionKey);
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
        private final PriorityBlockingQueue<ResponseContext> queue = new PriorityBlockingQueue<>(11, Comparator.comparingInt(ResponseContext::sequence));
        private int nextSequence = 1;
    }
}
