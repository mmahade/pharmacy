package com.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MedicineRequest(
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String manufacturer,
        @NotNull @Min(0) Integer minStock,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}
