package io.jacksoon.console.entity.service;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "service_instances",
        uniqueConstraints = {@UniqueConstraint(name = "uk_service_instance", columnNames = {"service_id", "instance_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceInstance {
    @Id
    @Column(name = "service_instance_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceInstanceId;
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "instance_id", nullable = false)
    private String instanceId;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "port", nullable = false)
    private int port;

    @Column(name = "protocol", nullable = false)
    private String protocol;

    @Column(name = "health_path")
    private String healthPath;

    public ServiceInstance(Long serviceId, String instanceId, String host, int port, String protocol, String healthPath) {
        this.serviceId = serviceId;
        this.instanceId = instanceId;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.healthPath = healthPath;
    }
}