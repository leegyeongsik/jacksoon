package io.jacksoon.common.util;
import java.util.concurrent.LinkedBlockingQueue;
public class CommonBlockingQueue<T> {
    LinkedBlockingQueue<T> queue= new LinkedBlockingQueue<>();

    public T take() throws InterruptedException {
        return queue.take();
    }
    public void put(T context ){
        queue.add(context);
    }
    public T poll(){
        return queue.poll();
    }
    public T peek(){
        return queue.peek();
    }
    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
