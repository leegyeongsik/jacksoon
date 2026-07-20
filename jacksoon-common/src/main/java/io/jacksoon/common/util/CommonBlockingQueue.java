package io.jacksoon.common.util;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CommonBlockingQueue<T> {
    LinkedBlockingQueue<T> queue= new LinkedBlockingQueue<>();

    public T take() throws InterruptedException {
        return queue.take();

    }
    public void put(T context ){
        queue.offer(context);
    }
    public T poll() throws InterruptedException {
        return queue.poll(3, TimeUnit.SECONDS);

    }
    public T peek(){
        return queue.peek();
    }
    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
