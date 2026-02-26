package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleItemRequest(
        @NotNull Long medicineId,
        @NotNull @Min(1) Integer quantity) {
}
