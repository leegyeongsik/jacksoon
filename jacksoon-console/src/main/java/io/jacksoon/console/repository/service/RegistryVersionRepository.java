package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.RegistryVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistryVersionRepository extends JpaRepository<RegistryVersion, Long> {
    Optional<RegistryVersion> findById(Long id);
}