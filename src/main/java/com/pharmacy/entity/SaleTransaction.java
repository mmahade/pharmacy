package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "sales")
public class SaleTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String saleNumber;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /**
     * Total amount paid so far (partial payments).
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    /**
     * Optional due date for unpaid/partial balance (invoice).
     */
    @Column
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String itemsSummary;

    @OneToMany(mappedBy = "sale", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserAccount createdBy;

    /**
     * Set when sale is created from completing a prescription; null for walk-in
     * sales.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @OneToMany(mappedBy = "sale", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SalePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "sale")
    private List<SaleReturn> returns = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (saleDate == null) {
            saleDate = LocalDate.now();
        }
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
    }
}
