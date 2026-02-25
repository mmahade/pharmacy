package com.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderRequest(
        @NotNull UUID supplierId,
        LocalDate orderDate,
        @NotEmpty(message = "At least one item required") List<@Valid PurchaseOrderItemRequest> items
) {
}
