package com.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/**
 * Per-line received quantities and batch info when receiving a PO.
 */
public record ReceivePurchaseOrderRequest(
        @NotEmpty(message = "At least one line receipt required") List<@Valid ReceivePurchaseOrderLineRequest> lines) {
    public record ReceivePurchaseOrderLineRequest(
            @NotNull Long purchaseOrderItemId,
            @Min(1) int quantityReceived,
            String batchNumber,
            LocalDate expiryDate) {
    }
}
