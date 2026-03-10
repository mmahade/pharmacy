package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicine product (per pharmacy): links to the master BdMedicine catalog,
 * plus pharmacy-specific selling price, min stock threshold, and batches.
 * Actual inventory is held in {@link StockBatch}.
 *
 * NOTE: name, category, manufacturer, genericName, dosageForm, strength,
 * description are stored in the {@link BdMedicine} master catalog.
 * Access them via {@code getMasterMedicine()}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "medicines",
        indexes = {
                @Index(name = "idx_medicine_pharmacy", columnList = "pharmacy_id"),
                @Index(name = "idx_pharmacy_medicine_name", columnList = "pharmacy_id,bd_medicine_id")
        }
)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer minStock;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "pharmacy_id", insertable = false, updatable = false)
    private Long pharmacyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    /** Link to the shared BD medicine master catalog (added by V3 migration). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bd_medicine_id", nullable = false)
    private BdMedicine masterMedicine;

    @OneToMany(mappedBy = "medicine")
    private List<StockBatch> batches = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (minStock == null) {
            minStock = 20;
        }
    }

    // -----------------------------------------------------------------------
    // Convenience delegators – read-through to BdMedicine master catalog
    // -----------------------------------------------------------------------

    public String getName() {
        return masterMedicine != null ? masterMedicine.getName() : null;
    }

    public String getCategory() {
        return masterMedicine != null ? masterMedicine.getCategory() : null;
    }

    public String getManufacturer() {
        return masterMedicine != null ? masterMedicine.getManufacturer() : null;
    }

    public String getGenericName() {
        return masterMedicine != null ? masterMedicine.getGenericName() : null;
    }

    public String getDosageForm() {
        return masterMedicine != null ? masterMedicine.getDosageForm() : null;
    }

    public String getStrength() {
        return masterMedicine != null ? masterMedicine.getStrength() : null;
    }

    public String getDescription() {
        return masterMedicine != null ? masterMedicine.getDescription() : null;
    }
}
