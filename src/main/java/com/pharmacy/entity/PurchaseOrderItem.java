package com.pharmacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantityOrdered;

    @Column(nullable = false)
    private Integer quantityReceived;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCostPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    /** Optional batch number for received stock (e.g. when receiving). */
    @Column
    private String batchNumber;
}
