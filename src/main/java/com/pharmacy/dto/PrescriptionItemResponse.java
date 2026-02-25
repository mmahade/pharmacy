package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PrescriptionItemResponse(
        UUID medicineId,
        String medicineName,
        Integer quantity,
        String instructions,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
