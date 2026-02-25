package com.pharmacy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(
        @NotNull UUID medicineId,
        @NotNull @Min(1) Integer quantityOrdered,
        @NotNull @DecimalMin("0.0") BigDecimal unitCostPrice
) {
}
