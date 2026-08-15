package io.jacksoon.common.selector;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
public class SelectorManager {
    ArrayList<Reactor> reactors = new ArrayList<>();
    int currentIdx = -1;
    public void init(int initSelector){
        for (int i = 0; i < initSelector; i++) {
            try {
                Selector selector = Selector.open();
                Reactor reactor=  new Reactor(selector);
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
        currentIdx+=1;
        if (currentIdx >= reactors.size()) {
            currentIdx = 0;
        }
        return reactors.get(currentIdx).selector;
    }
}
