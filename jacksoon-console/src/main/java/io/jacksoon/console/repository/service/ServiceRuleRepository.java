package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.ServiceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRuleRepository extends JpaRepository<ServiceRule,Long> {
    List<ServiceRule> findAllByServiceId(Long serviceId);
    void deleteAllByServiceId(Long serviceId);
}
