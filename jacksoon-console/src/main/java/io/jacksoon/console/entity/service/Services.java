package io.jacksoon.console.entity.service;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "services")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Services {

    @Id
    @Column(name = "service_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(name = "service_name", nullable = false, unique = true)
    private String serviceName;

    public Services( String serviceName) {
        this.serviceName = serviceName;
    }

}