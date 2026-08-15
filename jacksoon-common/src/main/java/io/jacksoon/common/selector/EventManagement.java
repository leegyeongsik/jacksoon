package io.jacksoon.common.selector;

import io.jacksoon.common.handler.Handler;

import java.util.HashMap;
import java.util.Map;

public class EventManagement {

    private final Map<Handler, EventState> states = new HashMap<>();
    private static class EventState {
        boolean processing;
        boolean rerun;
    }
    public synchronized boolean pendingEvent(Handler handler) {
        EventState state = states.computeIfAbsent(handler, h -> new EventState());
        if (state.processing) {
            state.rerun = true;
            return false;
        }
        state.processing = true;
        return true;
    }
    public synchronized boolean completeEvent(Handler handler) {
        EventState state = states.get(handler);
        if (state == null) {
            return false;
        }
        if (state.rerun) {
            state.rerun = false;
            return true;
        }

        states.remove(handler);
        return false;
    }

    public synchronized void remove(Handler handler) {
        states.remove(handler);
    }
}