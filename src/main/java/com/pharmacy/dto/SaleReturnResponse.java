package com.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record SaleReturnResponse(
        Long id,
        String returnNumber,
        LocalDate returnDate,
        BigDecimal totalAmount,
        String reason,
        Instant createdAt,
        Long saleId,
        String saleNumber,
        List<SaleReturnItemResponse> items) {
}
