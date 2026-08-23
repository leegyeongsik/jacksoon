package io.jacksoon.console.config;

import io.jacksoon.console.worker.queue.ProduceQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class PullConfig {
    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
    @Bean
    public ProduceQueue produceQueue(){
        return new ProduceQueue();
    }
    @Bean(destroyMethod = "shutdown")
    public ExecutorService filePullWorker() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
    }
    @Bean(destroyMethod = "shutdown")
    public ExecutorService pullWorker() {
        return new ThreadPoolExecutor(
                2,
                2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );
    }
}
