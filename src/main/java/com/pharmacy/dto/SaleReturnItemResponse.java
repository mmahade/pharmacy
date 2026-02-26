package com.pharmacy.dto;

import java.math.BigDecimal;

public record SaleReturnItemResponse(
        Long id,
        Long saleItemId,
        String medicineName,
        Integer quantityReturned,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
