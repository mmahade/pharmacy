package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleReturnItemResponse(
        UUID id,
        UUID saleItemId,
        String medicineName,
        Integer quantityReturned,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
