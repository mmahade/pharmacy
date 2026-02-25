package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Medicine product (master): name, category, selling price, min stock for alerts.
 * Actual inventory is held in {@link StockBatch} (same medicine can have multiple
 * batches with different expiry dates).
 */
@Getter
@Setter
@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue
    private UUID id;

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

    @Column(nullable = false)
    private Instant createdAt;

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
