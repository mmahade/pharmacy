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
                List<MedicineResponse> lowStock,
                List<ExpiryAlertItem> expiringSoon,
                List<PrescriptionResponse> recentPrescriptions,
                List<SaleResponse> recentSales) {

        public record DailyRevenue(
                        String date,
                        BigDecimal amount) {
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
