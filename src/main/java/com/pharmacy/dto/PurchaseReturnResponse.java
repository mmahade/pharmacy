package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PurchaseReturnResponse(
        Long id,
        String returnNumber,
        LocalDate returnDate,
        BigDecimal totalAmount,
        String reason,
        Instant createdAt,
        Long supplierId,
        String supplierName,
        Long purchaseOrderId,
        List<PurchaseReturnItemResponse> items) {
}
