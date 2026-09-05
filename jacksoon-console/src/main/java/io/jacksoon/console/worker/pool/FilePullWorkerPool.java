package io.jacksoon.console.worker.pool;

import io.jacksoon.console.repository.MetricFileOffsetRepository;
import io.jacksoon.console.worker.FilePullWorker;
import io.jacksoon.console.worker.queue.ProduceQueue;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

@Component
public class FilePullWorkerPool {
    private final Path metricRoot;
    private final MetricFileOffsetRepository metricFileOffsetRepository;
    private final ObjectMapper objectMapper;
    private final ProduceQueue produceQueue;
    private final ExecutorService filePullWorker;

    public FilePullWorkerPool(MetricFileOffsetRepository metricFileOffsetRepository, ObjectMapper objectMapper, ProduceQueue produceQueue, ExecutorService filePullWorker, @Value("${metric.directory}") String metricDirectory) {
        this.metricFileOffsetRepository = metricFileOffsetRepository;
        this.objectMapper = objectMapper;
        this.produceQueue = produceQueue;
        this.filePullWorker = filePullWorker;
        this.metricRoot = Path.of(metricDirectory);
    }

    @PostConstruct
    public void start() {
        for (int i = 0; i < 1; i++) {
            filePullWorker.submit(new FilePullWorker(metricRoot, metricFileOffsetRepository, objectMapper, produceQueue));
        }
    }
}
