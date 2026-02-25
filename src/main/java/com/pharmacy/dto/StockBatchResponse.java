package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StockBatchResponse(
        UUID id,
        String batchNumber,
        LocalDate expiryDate,
        Integer quantity,
        BigDecimal unitCostPrice,
        Instant receivedAt
) {
}
