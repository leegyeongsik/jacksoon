package io.jacksoon.console.worker.queue;

import io.jacksoon.console.dto.request.BaseProduceDto;

import java.util.concurrent.LinkedBlockingQueue;

public class ProduceQueue {
    LinkedBlockingQueue<BaseProduceDto> queue= new LinkedBlockingQueue<>();
    public BaseProduceDto take() throws InterruptedException {
        return queue.take();
    }
    public void put(BaseProduceDto context ){
        queue.offer(context);
    }
}
