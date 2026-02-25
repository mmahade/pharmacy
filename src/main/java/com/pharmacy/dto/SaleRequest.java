package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SaleRequest(
        LocalDate saleDate,
        @NotEmpty(message = "At least one item required") List<@Valid SaleItemRequest> items,
        @NotNull PaymentMethod paymentMethod,
        /** Optional due date for balance (invoice). */
        LocalDate dueDate,
        /** Optional initial payment (partial payment at sale time). */
        @DecimalMin("0.0") BigDecimal initialPaymentAmount,
        PaymentMethod initialPaymentMethod,
        String initialPaymentReference
) {
}
