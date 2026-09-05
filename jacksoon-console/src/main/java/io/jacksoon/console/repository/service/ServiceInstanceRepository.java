package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.ServiceInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceInstanceRepository extends JpaRepository<ServiceInstance,Long> {
    List<ServiceInstance> findAllByServiceId(Long serviceId);
    long countByServiceId(Long serviceId);
    void deleteByServiceIdAndInstanceId(Long serviceId, String instanceId);
    void deleteAllByServiceId(Long serviceId);

    boolean existsByServiceIdAndInstanceId(Long serviceId, String instanceId);
}
