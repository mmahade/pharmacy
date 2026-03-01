package com.pharmacy.service;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.dto.SaleResponse;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PrescriptionRepository;
import com.pharmacy.repository.SaleTransactionRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final TenantAccessService tenantAccessService;
        private final InventoryService inventoryService;
        private final MedicineRepository medicineRepository;
        private final StockBatchRepository stockBatchRepository;
        private final PrescriptionRepository prescriptionRepository;
        private final SaleTransactionRepository saleTransactionRepository;
        private final PrescriptionService prescriptionService;
        private final SalesService salesService;

        public DashboardSummaryResponse summary(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                LocalDate today = LocalDate.now();

                long totalMedicines = medicineRepository.countByPharmacy(pharmacy);
                long prescriptionsCount = prescriptionRepository.countByPharmacy(pharmacy);
                BigDecimal todayRevenue = saleTransactionRepository.totalForDay(pharmacy, today);

                // Calculate 7-day revenue trend
                List<DashboardSummaryResponse.DailyRevenue> revenueTrend = new java.util.ArrayList<>();
                java.time.format.DateTimeFormatter labelFormatter = java.time.format.DateTimeFormatter
                                .ofPattern("MMM dd");
                for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        BigDecimal dailyTotal = saleTransactionRepository.totalForDay(pharmacy, date);
                        revenueTrend.add(new DashboardSummaryResponse.DailyRevenue(
                                        date.format(labelFormatter),
                                        dailyTotal));
                }

                List<com.pharmacy.dto.MedicineResponse> allMeds = inventoryService.listMedicines(principal);
                long inStockCount = allMeds.stream().filter(m -> "In Stock".equals(m.status())).count();
                
                List<com.pharmacy.dto.MedicineResponse> lowStockMeds = inventoryService.getLowStockMedicines(principal);
                long lowStockCount = lowStockMeds.size();

                long outOfStockCount = inventoryService.getOutOfStockMedicines(principal).size();

                int expiryWindowDays = 30;
                List<StockBatch> expiringBatches = stockBatchRepository
                                .findByMedicine_PharmacyAndExpiryDateBetweenOrderByExpiryDateAsc(
                                                pharmacy, today, today.plusDays(expiryWindowDays));
                List<DashboardSummaryResponse.ExpiryAlertItem> expiringSoon = expiringBatches.stream()
                                .limit(10)
                                .map(b -> new DashboardSummaryResponse.ExpiryAlertItem(
                                                b.getMedicine().getName(),
                                                b.getMedicine().getId(),
                                                b.getBatchNumber(),
                                                b.getExpiryDate(),
                                                b.getQuantity(),
                                                (int) java.time.temporal.ChronoUnit.DAYS.between(today,
                                                                b.getExpiryDate())))
                                .toList();

                List<PrescriptionResponse> recentPrescriptions = prescriptionService.listPrescriptions(principal)
                                .stream()
                                .limit(5)
                                .toList();

                List<SaleResponse> recentSales = salesService.listSales(principal)
                                .stream()
                                .limit(10)
                                .toList();

                return new DashboardSummaryResponse(
                                totalMedicines,
                                prescriptionsCount,
                                todayRevenue,
                                revenueTrend,
                                inStockCount,
                                lowStockCount,
                                outOfStockCount,
                                lowStockMeds,
                                expiringSoon,
                                recentPrescriptions,
                                recentSales);
        }
}
