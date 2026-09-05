package io.jacksoon.console.entity.service;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "service_rules",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_service_rule_path", columnNames = {"service_id", "path_prefix"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRule {

    @Id
    @Column(name = "service_rule_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceRuleId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "path_prefix", nullable = false)
    private String pathPrefix;

    @Column(name = "strip_prefix", nullable = false)
    private boolean stripPrefix;

    public ServiceRule(Long serviceId, String pathPrefix, boolean stripPrefix) {
        this.serviceId = serviceId;
        this.pathPrefix = pathPrefix;
        this.stripPrefix = stripPrefix;
    }

}