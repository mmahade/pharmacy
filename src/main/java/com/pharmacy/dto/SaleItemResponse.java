package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemResponse(
        UUID medicineId,
        String medicineName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
