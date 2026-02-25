package com.pharmacy.dto;

import com.pharmacy.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
        UUID id,
        String orderNumber,
        LocalDate orderDate,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        UUID supplierId,
        String supplierName,
        List<PurchaseOrderItemResponse> items
) {
}
