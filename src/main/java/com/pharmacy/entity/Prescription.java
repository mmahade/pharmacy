package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String prescriptionNumber;

    @Column(nullable = false)
    private String patientName;

    @Column(nullable = false)
    private String doctorName;

    @Column(nullable = false)
    private LocalDate prescriptionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "prescription", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private UserAccount createdBy;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (prescriptionDate == null) {
            prescriptionDate = LocalDate.now();
        }
        if (status == null) {
            status = PrescriptionStatus.PENDING;
        }
    }
}
