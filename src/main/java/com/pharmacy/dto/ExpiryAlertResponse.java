package com.pharmacy.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ExpiryAlertResponse(
        String medicineName,
        UUID medicineId,
        String batchNumber,
        LocalDate expiryDate,
        Integer quantity,
        Integer daysUntilExpiry
) {
}
