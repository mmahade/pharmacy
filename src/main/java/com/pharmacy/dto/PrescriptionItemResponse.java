package com.pharmacy.dto;

import java.math.BigDecimal;

public record PrescriptionItemResponse(
        Long medicineId,
        String medicineName,
        Integer quantity,
        String instructions,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
