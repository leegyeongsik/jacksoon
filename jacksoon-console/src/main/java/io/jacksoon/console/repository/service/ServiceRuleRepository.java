package io.jacksoon.console.repository.service;

import io.jacksoon.console.entity.service.ServiceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRuleRepository extends JpaRepository<ServiceRule,Long> {
    Optional<ServiceRule> findByServiceIdAndPathPrefix(Long serviceId, String pathPrefix);
    void deleteAllByServiceId(Long serviceId);
}
