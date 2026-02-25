package com.pharmacy.dto;

import com.pharmacy.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        /** Invoice number (same as sale number). */
        String invoiceNumber,
        String saleNumber,
        LocalDate saleDate,
        String itemsSummary,
        List<SaleItemResponse> items,
        PaymentMethod paymentMethod,
        BigDecimal total,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        LocalDate dueDate,
        List<SalePaymentResponse> payments,
        List<SaleReturnResponse> returns,
        UUID prescriptionId,
        String prescriptionNumber
) {
}
