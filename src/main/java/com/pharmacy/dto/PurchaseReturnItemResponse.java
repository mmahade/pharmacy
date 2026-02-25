package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseReturnItemResponse(
        UUID id,
        UUID medicineId,
        String medicineName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
