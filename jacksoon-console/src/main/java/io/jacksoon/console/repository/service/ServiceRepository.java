package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.Services;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Services,Long> {
    Optional<Services> findByServiceName(String serviceName);

}
