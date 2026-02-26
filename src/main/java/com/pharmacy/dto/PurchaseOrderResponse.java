package com.pharmacy.dto;

import com.pharmacy.entity.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String orderNumber,
        LocalDate orderDate,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Long supplierId,
        String supplierName,
        List<PurchaseOrderItemResponse> items) {
}
