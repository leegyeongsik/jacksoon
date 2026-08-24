package io.jacksoon.console.worker;

import io.jacksoon.console.dto.request.BaseProduceDto;
import io.jacksoon.console.entity.metric.MetricFileOffset;
import io.jacksoon.console.repository.MetricFileOffsetRepository;
import io.jacksoon.console.worker.queue.ProduceQueue;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class FilePullWorker implements Runnable {
    private static final String ROUTER_METRIC_FILE = "router.metric";
    private final WatchService watchService;
    private final Path metricRoot;
    private final Map<WatchKey, Path> watchPaths = new HashMap<>();
    private final MetricFileOffsetRepository metricFileOffsetRepository;
    private final ObjectMapper objectMapper;
    private final ProduceQueue produceQueue;

    public FilePullWorker(Path metricRoot, MetricFileOffsetRepository metricFileOffsetRepository, ObjectMapper objectMapper, ProduceQueue produceQueue) {
        this.metricRoot = metricRoot;
        this.metricFileOffsetRepository = metricFileOffsetRepository;
        this.objectMapper = objectMapper;
        this.produceQueue = produceQueue;
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            registerRoot();
            registerTodayIfExists();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerRoot() throws IOException {
        Files.createDirectories(metricRoot);
        WatchKey key = metricRoot.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        watchPaths.put(key, metricRoot);
    }

    private void registerTodayIfExists() throws IOException {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Path todayRoot = metricRoot.resolve(today.toString());
        if (Files.isDirectory(todayRoot)) {
            registerMetricDirectory(todayRoot);
            readIfMetricAlreadyExists(todayRoot);
        }
    }

    private void registerMetricDirectory(Path directory) throws IOException {
        WatchKey key = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        watchPaths.put(key, directory);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.take();
                Path watchedDirectory = watchPaths.get(key);
                if (watchedDirectory == null) {
                    key.reset();
                    continue;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path context = (Path) event.context();
                    Path changed = watchedDirectory.resolve(context);
                    if (watchedDirectory.equals(metricRoot) && Files.isDirectory(changed)) {
                        registerMetricDirectory(changed);
                        readIfMetricAlreadyExists(changed);
                        continue;
                    }
                    if (changed.getFileName().toString().equals(ROUTER_METRIC_FILE)) {
                        pull(changed);
                    }
                }
                if (!key.reset()) {
                    watchPaths.remove(key);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readIfMetricAlreadyExists(Path directory) {
        Path file = directory.resolve(ROUTER_METRIC_FILE);
        if (Files.exists(file)) {
            pull(file);
        }
    }

    private void pull(Path file) {
        LocalDate metricDate = LocalDate.parse(file.getParent().getFileName().toString());
        String fileName = file.getFileName().toString();
        MetricFileOffset fileOffset = metricFileOffsetRepository.findByMetricDateAndFileName(metricDate, fileName).orElseGet(() -> new MetricFileOffset(metricDate, fileName, 0L));
        long offset = fileOffset.getOffset();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
            long fileLength = randomAccessFile.length();
            if (offset > fileLength) {
                offset = 0L;
            }
            if (offset == fileLength) {
                return;
            }
            randomAccessFile.seek(offset);
            long newOffset = offset;

            while (true) {
                long lineStart = randomAccessFile.getFilePointer();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int value;
                boolean completed = false;
                while ((value = randomAccessFile.read()) != -1) {
                    if (value == '\n') {
                        completed = true;
                        break;
                    }
                    buffer.write(value);
                }
                if (!completed) {
                    randomAccessFile.seek(lineStart);
                    break;
                }
                byte[] lineBytes = buffer.toByteArray();
                int length = lineBytes.length;
                if (length > 0 && lineBytes[length - 1] == '\r') {
                    length--;
                }
                String line = new String(lineBytes, 0, length, StandardCharsets.UTF_8);
                if (line.isBlank()) {
                    newOffset = randomAccessFile.getFilePointer();
                    continue;
                }
                BaseProduceDto dto = objectMapper.readValue(line, BaseProduceDto.class);
                throwDto(dto);
                newOffset = randomAccessFile.getFilePointer();
            }
            fileOffset.updateOffset(newOffset);

            metricFileOffsetRepository.save(fileOffset);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwDto(BaseProduceDto dto) {
        produceQueue.put(dto);
    }
}