package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String licenseNumber;

    @Column
    private String phoneNumber;

    @Column
    private String address;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pharmacy")
    private Set<UserAccount> users = new HashSet<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
