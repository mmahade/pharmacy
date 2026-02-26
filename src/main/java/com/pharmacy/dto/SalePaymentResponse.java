package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public record SalePaymentResponse(
        Long id,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Instant paidAt,
        String reference) {
}
