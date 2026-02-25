package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseReturnItemRequest(
        @NotNull UUID medicineId,
        @NotNull @Min(1) Integer quantity,
        @NotNull BigDecimal unitPrice
) {
}
