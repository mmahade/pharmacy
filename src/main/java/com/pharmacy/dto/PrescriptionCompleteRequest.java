package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PrescriptionCompleteRequest(
    @NotNull PaymentMethod paymentMethod,
    @NotNull BigDecimal amountPaid,
    String paymentReference,
    LocalDate dueDate
) {
}
