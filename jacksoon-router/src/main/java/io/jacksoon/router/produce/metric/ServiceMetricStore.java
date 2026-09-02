package io.jacksoon.router.produce.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ServiceMetricStore {
    private final Map<String, Counter> counterMap = new ConcurrentHashMap<>();

    public void success(String serviceName) {
        counter(serviceName).success.increment();
    }
    public void failure(String serviceName) {
        counter(serviceName).failure.increment();
    }
    public List<ServiceMetricSnapshot> snapshot() {
        List<ServiceMetricSnapshot> snapshots = new ArrayList<>(counterMap.size());
        for (Map.Entry<String, Counter> entry : counterMap.entrySet()) {
            Counter counter = entry.getValue();
            snapshots.add(new ServiceMetricSnapshot(entry.getKey(), counter.success.sum(), counter.failure.sum()));
        }
        return snapshots;
    }
    private Counter counter(String serviceName) {
        return counterMap.computeIfAbsent(serviceName, ignored -> new Counter());
    }

    private static final class Counter {
        private final LongAdder success = new LongAdder();
        private final LongAdder failure = new LongAdder();
    }

    public record ServiceMetricSnapshot(String serviceName, long success, long failure) {
    }
}