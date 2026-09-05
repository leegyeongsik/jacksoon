package io.jacksoon.common.produce.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;

import java.util.ArrayList;
import java.util.List;

public class ProduceWorker<T extends ProduceDto> implements Runnable { // 생각났다 여기서 ProduceDto를 전부 다 받을 수 있냐 아 그래서 wokker를 갖고올때 저걸 다 받게 할 수 있냐
    protected final int batchSize = 500; // 그래서 큐에 ProduceDto를 걸어 놓고 저거를 받게 할 수 있냐 받게할수있지 queue는 뭘 주입받지않고 타입만 생성하니까
    protected final List<T> buffer = new ArrayList<>(); // 그리고 주입받는다고 하더라도 t랑 상관없지 인터페이스를 걸어놔서 주입받게 하더라도 인터페이스에 연관된거 다 갖과어서 체크해서 주입하니까
    protected final CommonBlockingQueue<T> queue;
    protected final ProduceStore<T> produceStore;
    private final ExceptionDispatcher exceptionDispatcher;
    public ProduceWorker(CommonBlockingQueue<T> queue, ProduceStore<T> produceStore, ExceptionDispatcher exceptionDispatcher) {
        this.queue = queue;
        this.produceStore = produceStore;
        this.exceptionDispatcher = exceptionDispatcher;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    T dto = queue.poll();
                    if (dto != null) {
                        buffer.add(dto);
                    }
                    boolean sizeTrigger = buffer.size() >= batchSize;
                    boolean timeoutTrigger = dto == null && !buffer.isEmpty();
                    if (sizeTrigger || timeoutTrigger) {
                        flush();
                    }
                } catch (Exception e) {
                    dispatchException(e);
                    // 여기서 파일로 저장해놓는다던지 그런식으로 처리해야할듯
                }
            }
        } finally {
            try {
                flush();
            } catch (Exception e) {
                dispatchException(e);
                // 여기서 파일로 저장해놓는다던지 그런식으로 처리해야할듯 얘도
                buffer.clear();
            }
        }
    }
    protected void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        List<T> batch = new ArrayList<>(buffer);

        saveAll(batch);
        buffer.subList(0, batch.size()).clear();
    }
    public void saveAll(List<T> batch) {
        produceStore.saveAll(batch);
    }
    private void dispatchException(Throwable throwable) {
        if (exceptionDispatcher == null) {
            throwable.printStackTrace();
            return;
        }
        exceptionDispatcher.dispatch(throwable);
    }
}
