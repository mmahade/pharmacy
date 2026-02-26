package com.pharmacy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SaleReturnItemRequest(
        @NotNull Long saleItemId,
        @NotNull @Min(1) Integer quantityReturned) {
}
