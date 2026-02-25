package com.pharmacy.service;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.dto.SaleResponse;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PrescriptionRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.repository.SaleTransactionRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TenantAccessService tenantAccessService;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final PrescriptionService prescriptionService;
    private final SalesService salesService;

    public DashboardSummaryResponse summary(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);

        long totalMedicines = medicineRepository.countByPharmacy(pharmacy);
        long prescriptionsCount = prescriptionRepository.countByPharmacy(pharmacy);
        BigDecimal todayRevenue = saleTransactionRepository.totalForDay(pharmacy, LocalDate.now());

        List<Medicine> allMeds = medicineRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy);
        List<DashboardSummaryResponse.LowStockItem> lowStock = allMeds.stream()
                .map(m -> {
                    int total = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(m).stream().mapToInt(b -> b.getQuantity()).sum();
                    return new DashboardSummaryResponse.LowStockItem(m.getName(), total, m.getMinStock());
                })
                .filter(l -> l.stock() <= l.minStock())
                .limit(5)
                .toList();

        LocalDate today = LocalDate.now();
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
                        (int) java.time.temporal.ChronoUnit.DAYS.between(today, b.getExpiryDate())))
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
                lowStock.size(),
                lowStock,
                expiringSoon,
                recentPrescriptions,
                recentSales
        );
    }
}
