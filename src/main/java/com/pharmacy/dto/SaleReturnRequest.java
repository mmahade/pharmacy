package com.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SaleReturnRequest(
                @NotNull(message = "Sale ID is required") Long saleId,
                LocalDate returnDate,
                String reason,
                @NotEmpty(message = "At least one item required") List<@Valid SaleReturnItemRequest> items) {
}
