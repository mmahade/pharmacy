package com.pharmacy.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
                long totalMedicines,
                long prescriptionsCount,
                BigDecimal todayRevenue,
                List<DailyRevenue> revenueTrend,
                long inStockCount,
                long lowStockCount,
                long outOfStockCount,
                List<LowStockItem> lowStock,
                List<ExpiryAlertItem> expiringSoon,
                List<PrescriptionResponse> recentPrescriptions,
                List<SaleResponse> recentSales) {

        public record DailyRevenue(
                        String date,
                        BigDecimal amount) {
        }

        public record LowStockItem(
                        String name,
                        Integer stock,
                        Integer minStock) {
        }

        /**
         * Batch expiring within the configured window (e.g. 30 days).
         */
        public record ExpiryAlertItem(
                        String medicineName,
                        Long medicineId,
                        String batchNumber,
                        java.time.LocalDate expiryDate,
                        Integer quantity,
                        Integer daysUntilExpiry) {
        }
}
