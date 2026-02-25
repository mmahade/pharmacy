package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
        UUID id,
        UUID medicineId,
        String medicineName,
        Integer quantityOrdered,
        Integer quantityReceived,
        BigDecimal unitCostPrice,
        BigDecimal lineTotal,
        String batchNumber
) {
}
