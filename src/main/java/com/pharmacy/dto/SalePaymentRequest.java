package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SalePaymentRequest(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentMethod paymentMethod,
        String reference
) {
}
