package io.jacksoon.router.worker;

import io.jacksoon.common.exception.ExceptionDispatcher;
import io.jacksoon.common.produce.dto.ProduceDto;
import io.jacksoon.common.util.CommonBlockingQueue;
import io.jacksoon.router.exception.RouterMetricException;
import io.jacksoon.router.produce.metric.ServiceMetricStore;
import io.jacksoon.router.produce.metric.ServiceMetricStore.ServiceMetricSnapshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ProduceMetricWorker implements Runnable {

    private final long FLUSH_INTERVAL_MILLIS;
    private final ServiceMetricStore metricStore;
    private final CommonBlockingQueue<ProduceDto> produceDtoQueue;
    private final ExceptionDispatcher exceptionDispatcher;
    private final Constructor<?> constructor;
    private final Map<String, LastMetric> lastMetricMap = new HashMap<>();
    public ProduceMetricWorker(ServiceMetricStore metricStore, CommonBlockingQueue<ProduceDto> produceDtoQueue, Class<?> clazz, ExceptionDispatcher exceptionDispatcher,long flushIntervalMillis) {
        FLUSH_INTERVAL_MILLIS = flushIntervalMillis;
        this.metricStore = metricStore;
        this.produceDtoQueue = produceDtoQueue;
        this.exceptionDispatcher = exceptionDispatcher;
        try {
            this.constructor = clazz.getConstructor(String.class, long.class, long.class);
        } catch (NoSuchMethodException e) {
            throw new RouterMetricException("Metric DTO constructor not found. class=" + clazz.getName(), e);
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(FLUSH_INTERVAL_MILLIS);
                    flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    exceptionDispatcher.dispatch(e);
                }
            }
        } finally {
            try {
                flush();
            } catch (Exception e) {
                exceptionDispatcher.dispatch(e);
            }
        }
    }

    private void flush() {
        for (ServiceMetricSnapshot snapshot : metricStore.snapshot()) {
            LastMetric last = lastMetricMap.computeIfAbsent(snapshot.serviceName(), ignored -> new LastMetric());
            long success = snapshot.success() - last.success;
            long failure = snapshot.failure() - last.failure;
            if (success == 0 && failure == 0) {
                continue;
            }
            ProduceDto produceDto = createProduceDto(snapshot.serviceName(), success, failure);
            produceDtoQueue.put(produceDto);
            last.success = snapshot.success();
            last.failure = snapshot.failure();
        }
    }
    private ProduceDto createProduceDto(String serviceName, long success, long failure) {
        try {
            return (ProduceDto) constructor.newInstance(serviceName, success, failure);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RouterMetricException("Failed to create metric dto. serviceName=" + serviceName, e);
        }
    }
    private static final class LastMetric {
        private long success;
        private long failure;
    }
}