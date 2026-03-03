package com.pharmacy.dto;

import java.math.BigDecimal;

import java.time.LocalDate;

public record PurchaseReturnItemResponse(
                Long id,
                Long medicineId,
                String medicineName,
                Long batchId,
                String batchNumber,
                LocalDate expiryDate,
                Integer quantity,
                BigDecimal unitPrice,
                BigDecimal lineTotal) {
}
