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

import java.util.UUID;

/** Links a sale line to the batch(es) it was fulfilled from (for returns). */
@Getter
@Setter
@Entity
@Table(name = "sale_item_allocations")
public class SaleItemAllocation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_batch_id", nullable = false)
    private StockBatch stockBatch;

    @Column(nullable = false)
    private Integer quantity;
}
