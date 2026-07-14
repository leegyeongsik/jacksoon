package io.jacksoon.common.util;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommonBlockingQueue<T> {
    LinkedBlockingQueue<T> queue= new LinkedBlockingQueue<>();

    public T take()  {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void put(T context ){
        queue.add(context);
    }
    public T poll()  {
        try {
            return queue.poll(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public T peek(){
        return queue.peek();
    }
    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
