package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        long totalMedicines,
        long prescriptionsCount,
        BigDecimal todayRevenue,
        long lowStockAlerts,
        List<LowStockItem> lowStockItems,
        List<ExpiryAlertItem> expiringSoon,
        List<PrescriptionResponse> recentPrescriptions,
        List<SaleResponse> recentSales
) {
    public record LowStockItem(
            String medicineName,
            Integer stock,
            Integer minStock
    ) {
    }

    /** Batch expiring within the configured window (e.g. 30 days). */
    public record ExpiryAlertItem(
            String medicineName,
            java.util.UUID medicineId,
            String batchNumber,
            java.time.LocalDate expiryDate,
            Integer quantity,
            Integer daysUntilExpiry
    ) {
    }
}
