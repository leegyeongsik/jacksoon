package io.jacksoon.common.produce.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.produce.dto.ProduceDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class FileStore implements ProduceStore<ProduceDto>{
    private final ObjectMapper objectMapper;
    Path metricRoot = Path.of(System.getProperty("jacksoon.metric.root", "D:/jacksoon/metric"));
    private final Object stateLock = new Object();

    public FileStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveAll(List<ProduceDto> buffers) {
        synchronized (stateLock){
            Path file = getTodayRoot().resolve("router.metric");
            try {
                StringBuilder builder = new StringBuilder();
                for (ProduceDto buffer : buffers) {builder.append(objectMapper.writeValueAsString(buffer)).append(System.lineSeparator());}
                Files.writeString(file, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public Path getTodayRoot() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Path todayRoot = metricRoot.resolve(today.toString());
        try {
            Files.createDirectories(todayRoot);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return todayRoot;
    }
}
