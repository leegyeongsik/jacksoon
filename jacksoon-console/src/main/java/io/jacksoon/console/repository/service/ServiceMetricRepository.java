package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.ServiceMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceMetricRepository extends JpaRepository<ServiceMetric,Long> {
    void deleteAllByServiceId(Long serviceId);
}
