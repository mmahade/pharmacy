package com.pharmacy.dto;

import java.math.BigDecimal;

public record SaleItemResponse(
                Long id,
                Long medicineId,
                String medicineName,
                Integer quantity,
                BigDecimal unitPrice,
                BigDecimal lineTotal) {
}
