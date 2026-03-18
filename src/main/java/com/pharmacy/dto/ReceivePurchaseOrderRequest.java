package com.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Per-line received quantities and batch info when receiving a PO.
 * If {@code finalizeOrder} is {@code true} the order will be marked RECEIVED
 * even when some lines are still short-delivered (supplier cannot fulfil remainder).
 */
public record ReceivePurchaseOrderRequest(
        @NotEmpty(message = "At least one line receipt required") List<@Valid ReceivePurchaseOrderLineRequest> lines,
        String paymentMethod,
        String paymentReference,
        BigDecimal discountAmount,
        BigDecimal discountPercentage,
        BigDecimal totalPaymentAmount,
        Boolean finalizeOrder) {
    public record ReceivePurchaseOrderLineRequest(
            @NotNull Long purchaseOrderItemId,
            @Min(0) int quantityReceived,
            String batchNumber,
            @NotNull(message = "Expiry date is required") LocalDate expiryDate) {
    }
}
