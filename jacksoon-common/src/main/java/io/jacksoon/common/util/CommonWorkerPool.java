package io.jacksoon.common.util;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
public class CommonWorkerPool<T extends Runnable> {
    private final int workerCount;
    private final Supplier<T> workerSupplier;
    private final List<Thread> threads = new ArrayList<>();

    public CommonWorkerPool(int workerCount, Supplier<T> workerSupplier) {
        this.workerCount = workerCount;
        this.workerSupplier = workerSupplier;
    }

    public void start() {
        for (int i = 0; i < workerCount; i++) {
            T worker = workerSupplier.get();
            Thread thread = new Thread(worker);
            threads.add(thread);
            thread.start();
        }
    }
}