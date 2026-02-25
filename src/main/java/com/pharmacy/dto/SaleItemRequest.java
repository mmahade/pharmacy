package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaleItemRequest(
        @NotNull UUID medicineId,
        @NotNull @Min(1) Integer quantity
) {
}
