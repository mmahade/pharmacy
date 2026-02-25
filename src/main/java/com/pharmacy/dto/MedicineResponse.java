package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MedicineResponse(
        UUID id,
        String name,
        String category,
        String manufacturer,
        Integer totalStock,
        Integer minStock,
        BigDecimal price,
        LocalDate earliestExpiry,
        String status,
        List<StockBatchResponse> batches
) {
}
