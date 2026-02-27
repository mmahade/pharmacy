package com.pharmacy.dto;

import java.math.BigDecimal;

public record InventoryStatsResponse(
        long totalMedicines,
        long lowStockCount,
        long totalStockUnits,
        BigDecimal totalValue) {
}
