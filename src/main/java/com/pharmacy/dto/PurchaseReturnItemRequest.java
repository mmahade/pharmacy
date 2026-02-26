package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseReturnItemRequest(
        @NotNull Long medicineId,
        @NotNull @Min(1) Integer quantity,
        @NotNull BigDecimal unitPrice) {
}
