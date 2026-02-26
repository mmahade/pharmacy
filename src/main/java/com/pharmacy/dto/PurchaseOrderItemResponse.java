package com.pharmacy.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long medicineId,
        String medicineName,
        Integer quantityOrdered,
        Integer quantityReceived,
        BigDecimal unitCostPrice,
        BigDecimal lineTotal,
        String batchNumber) {
}
