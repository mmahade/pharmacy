package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
