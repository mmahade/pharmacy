package com.pharmacy.service;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.dto.SaleResponse;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PrescriptionRepository;
import com.pharmacy.repository.SaleReturnRepository;
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
        private final SaleReturnRepository saleReturnRepository;
        private final PrescriptionService prescriptionService;
        private final SalesService salesService;

        public DashboardSummaryResponse summary(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                LocalDate today = LocalDate.now();

                long totalMedicines = medicineRepository.countByPharmacyId(pharmacy.getId());
                long prescriptionsCount = prescriptionRepository.countByPharmacyId(pharmacy.getId());
                
                BigDecimal todaySales = saleTransactionRepository.totalForDay(pharmacy, today);
                BigDecimal todayReturns = saleReturnRepository.totalForDay(pharmacy, today);
                BigDecimal todayRevenue = todaySales.subtract(todayReturns);

                // Calculate 7-day revenue trend
                List<DashboardSummaryResponse.DailyRevenue> revenueTrend = new java.util.ArrayList<>();
                java.time.format.DateTimeFormatter labelFormatter = java.time.format.DateTimeFormatter
                                .ofPattern("MMM dd");
                for (int i = 6; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        BigDecimal dailySales = saleTransactionRepository.totalForDay(pharmacy, date);
                        BigDecimal dailyReturns = saleReturnRepository.totalForDay(pharmacy, date);
                        BigDecimal netDaily = dailySales.subtract(dailyReturns);
                        
                        revenueTrend.add(new DashboardSummaryResponse.DailyRevenue(
                                        date.format(labelFormatter),
                                        netDaily));
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
                                .limit(5)
                                .toList();

                List<DashboardSummaryResponse.TopSellingMedicine> topSellingMedicines = saleTransactionRepository
                                .findTopSellingMedicines(pharmacy, org.springframework.data.domain.PageRequest.of(0, 5))
                                .stream()
                                .map(p -> new DashboardSummaryResponse.TopSellingMedicine(
                                                p.getMedicineName(),
                                                p.getTotalQuantity(),
                                                p.getTotalRevenue()))
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
                                recentSales,
                                topSellingMedicines);
        }
}
