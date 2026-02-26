package com.pharmacy.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Links a sale line to the batch(es) it was fulfilled from (for returns).
 */
@Getter
@Setter
@Entity
@Table(name = "sale_item_allocations")
public class SaleItemAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_item_id", nullable = false)
    private SaleItem saleItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_batch_id", nullable = false)
    private StockBatch stockBatch;

    @Column(nullable = false)
    private Integer quantity;
}
