package com.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record SaleReturnRequest(
        LocalDate returnDate,
        String reason,
        @NotEmpty(message = "At least one item required") List<@Valid SaleReturnItemRequest> items
) {
}
