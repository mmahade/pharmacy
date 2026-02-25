package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleTransaction sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @OneToMany(mappedBy = "saleItem", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemAllocation> allocations = new ArrayList<>();
}
