package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Master catalog of medicines available in Bangladesh.
 * This is meant to be a read-heavy lookup table for pharmacies to pull from.
 */
@Getter
@Setter
@Entity
@Table(
        name = "bd_medicines",
        indexes = {
                @Index(name = "idx_bd_medicine_name", columnList = "name"),
                @Index(name = "idx_bd_generic_name", columnList = "generic_name"),
                @Index(name = "idx_bd_category", columnList = "category")
        }
)
public class BdMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String manufacturer;

    @Column
    private String genericName;

    @Column
    private String dosageForm;

    @Column
    private String strength;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
