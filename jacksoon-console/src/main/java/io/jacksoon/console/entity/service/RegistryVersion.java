package io.jacksoon.console.entity.service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "registry_version")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegistryVersion {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private long version;

    public RegistryVersion(long version) {
        this.id = SINGLETON_ID;
        this.version = version;
    }

    public void updateVersion(long version) {
        this.version = version;
    }
}