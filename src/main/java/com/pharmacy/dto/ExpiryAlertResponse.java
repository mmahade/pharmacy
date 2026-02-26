package com.pharmacy.dto;

import java.time.LocalDate;

public record ExpiryAlertResponse(
        String medicineName,
        Long medicineId,
        String batchNumber,
        LocalDate expiryDate,
        Integer quantity,
        Integer daysUntilExpiry) {
}
