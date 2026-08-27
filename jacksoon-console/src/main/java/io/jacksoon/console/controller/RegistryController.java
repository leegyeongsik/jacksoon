package io.jacksoon.console.controller;

import io.jacksoon.console.dto.response.RegistrySnapshot;
import io.jacksoon.console.service.ConsoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registry")
public class RegistryController {
    private final ConsoleService consoleService;

    public RegistryController(ConsoleService consoleService) {
        this.consoleService = consoleService;
    }

    @GetMapping("/snapshot")
    public ResponseEntity<RegistrySnapshot> snapshot() {
        return ResponseEntity.ok(consoleService.getRegistrySnapshot());
    }
}
