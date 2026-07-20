package io.jacksoon.console.controller;

import io.jacksoon.console.dto.request.BaseProduceDto;
import io.jacksoon.console.event.EventRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConsoleController {
    private final EventRegistry registry;
    public ConsoleController(EventRegistry registry) {
        this.registry = registry;
    }
    @PostMapping("/consumer")
    ResponseEntity<Void> consumer(@RequestBody List<BaseProduceDto> produceDtoList){
        for (BaseProduceDto baseProduceDto : produceDtoList) {
            registry.execute(baseProduceDto);
        }
        return ResponseEntity.ok(null);
    }
}
