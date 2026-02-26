package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StockBatchResponse(
        Long id,
        String batchNumber,
        LocalDate expiryDate,
        Integer quantity,
        BigDecimal unitCostPrice,
        Instant receivedAt) {
}
