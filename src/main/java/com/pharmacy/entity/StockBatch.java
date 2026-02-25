package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One batch of a medicine: same product can have multiple batches with different
 * batch numbers and expiry dates (e.g. different delivery dates).
 */
@Getter
@Setter
@Entity
@Table(name = "stock_batches")
public class StockBatch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal unitCostPrice;

    @Column(nullable = false)
    private Instant receivedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }
}
