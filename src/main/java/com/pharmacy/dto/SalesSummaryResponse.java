package com.pharmacy.dto;

import java.math.BigDecimal;

public record SalesSummaryResponse(
    long todaySalesCount,
    BigDecimal todayRevenue,
    BigDecimal pendingBalance,
    long returnsCount30d
) {
}
