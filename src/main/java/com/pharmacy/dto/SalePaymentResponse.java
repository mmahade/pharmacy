package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SalePaymentResponse(
        UUID id,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant paidAt,
        String reference
) {
}
