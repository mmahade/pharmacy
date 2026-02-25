package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaleReturnItemRequest(
        @NotNull UUID saleItemId,
        @NotNull @Min(1) Integer quantityReturned
) {
}
