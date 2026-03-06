package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicine product (master): name, category, selling price, min stock for
 * alerts.
 * Actual inventory is held in {@link StockBatch} (same medicine can have
 * multiple
 * batches with different expiry dates).
 */
@Getter
@Setter
@Entity
@Table(
        name = "medicines",
        indexes = {
                @Index(name = "idx_medicine_pharmacy", columnList = "pharmacy_id"),
                @Index(name = "idx_pharmacy_medicine_name", columnList = "pharmacy_id,name"),
                @Index(name = "idx_pharmacy_generic_name", columnList = "pharmacy_id,generic_name"),
                @Index(name = "idx_pharmacy_category", columnList = "pharmacy_id,category")
        }
)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Integer minStock;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

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

    @Column(name = "pharmacy_id", insertable = false, updatable = false)
    private Long pharmacyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @OneToMany(mappedBy = "medicine")
    private List<StockBatch> batches = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (minStock == null) {
            minStock = 20;
        }
    }
}
