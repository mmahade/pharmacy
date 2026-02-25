package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseReturnResponse(
        UUID id,
        String returnNumber,
        LocalDate returnDate,
        BigDecimal totalAmount,
        String reason,
        Instant createdAt,
        UUID supplierId,
        String supplierName,
        UUID purchaseOrderId,
        List<PurchaseReturnItemResponse> items
) {
}
