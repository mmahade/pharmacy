package com.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockBatchRequest(
        @NotBlank String batchNumber,
        @NotNull LocalDate expiryDate,
        @NotNull @Min(1) Integer quantity,
        @DecimalMin("0.0") BigDecimal unitCostPrice
) {
}
