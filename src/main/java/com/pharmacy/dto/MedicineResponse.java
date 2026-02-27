package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MedicineResponse(
                Long id,
                String name,
                String category,
                String manufacturer,
                Integer totalStock,
                Integer minStock,
                BigDecimal price,
                String genericName,
                String dosageForm,
                String strength,
                String description,
                LocalDate earliestExpiry,
                String status,
                List<StockBatchResponse> batches) {
}
