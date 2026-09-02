package io.jacksoon.common.selector;

import io.jacksoon.common.exception.ExceptionDispatcher;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorManager {
    private final ExceptionDispatcher exceptionDispatcher;
    ArrayList<Reactor> reactors = new ArrayList<>();
    private final AtomicInteger currentIdx = new AtomicInteger();

    public SelectorManager(ExceptionDispatcher exceptionDispatcher) {
        this.exceptionDispatcher = exceptionDispatcher;
    }

    public void init(int initSelector){
        for (int i = 0; i < initSelector; i++) {
            try {
                Selector selector = Selector.open();
                Reactor reactor=  new Reactor(selector,exceptionDispatcher);
                reactors.add(reactor);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        for (Reactor reactor : reactors) {
            new Thread(reactor).start();
        }
    }
    public Selector nextSelector() {
        int index = Math.floorMod(currentIdx.getAndIncrement(), reactors.size());
        return reactors.get(index).selector;
    }
}