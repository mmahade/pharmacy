package com.pharmacy.service;

import com.pharmacy.dto.*;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TenantAccessService tenantAccessService;

    public List<MedicineResponse> listMedicines(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return medicineRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MedicineResponse> searchMedicines(AppUserPrincipal principal, String query) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        if (query == null || query.isBlank()) {
            return List.of();
        }
        var page = PageRequest.of(0, 25);
        return medicineRepository.findByPharmacyAndNameContainingIgnoreCaseOrderByNameAsc(
                        pharmacy, query.trim(), page)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineResponse createMedicine(AppUserPrincipal principal, MedicineRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Medicine medicine = new Medicine();
        medicine.setPharmacy(pharmacy);
        medicine.setName(request.name().trim());
        medicine.setCategory(request.category().trim());
        medicine.setManufacturer(request.manufacturer().trim());
        medicine.setMinStock(request.minStock());
        medicine.setPrice(request.price());
        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineResponse addBatch(AppUserPrincipal principal, Long medicineId, StockBatchRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Medicine medicine = medicineRepository.findByIdAndPharmacy(medicineId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Medicine not found"));
        StockBatch batch = stockBatchRepository
                .findByMedicineAndBatchNumber(medicine, request.batchNumber().trim())
                .orElseGet(() -> {
                    StockBatch b = new StockBatch();
                    b.setMedicine(medicine);
                    b.setBatchNumber(request.batchNumber().trim());
                    b.setExpiryDate(request.expiryDate());
                    b.setUnitCostPrice(request.unitCostPrice());
                    b.setQuantity(0);
                    return b;
                });
        batch.setExpiryDate(request.expiryDate());
        if (request.unitCostPrice() != null) {
            batch.setUnitCostPrice(request.unitCostPrice());
        }
        batch.setQuantity(batch.getQuantity() + request.quantity());
        stockBatchRepository.save(batch);
        return toResponse(medicine);
    }

    /**
     * List all batches for a medicine (e.g. for detail page).
     */
    public List<StockBatchResponse> listBatchesForMedicine(AppUserPrincipal principal, Long medicineId) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Medicine medicine = medicineRepository.findByIdAndPharmacy(medicineId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Medicine not found"));
        return stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine).stream()
                .map(b -> new StockBatchResponse(b.getId(), b.getBatchNumber(), b.getExpiryDate(),
                        b.getQuantity(), b.getUnitCostPrice(), b.getReceivedAt()))
                .toList();
    }

    /**
     * Batches expiring within the given days (for alerts / expiry report).
     */
    public List<ExpiryAlertResponse> getExpiryAlerts(AppUserPrincipal principal, int withinDays) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(Math.max(1, withinDays));
        return stockBatchRepository
                .findByMedicine_PharmacyAndExpiryDateBetweenOrderByExpiryDateAsc(pharmacy, today, end)
                .stream()
                .map(b -> new ExpiryAlertResponse(
                        b.getMedicine().getName(),
                        b.getMedicine().getId(),
                        b.getBatchNumber(),
                        b.getExpiryDate(),
                        b.getQuantity(),
                        (int) ChronoUnit.DAYS.between(today, b.getExpiryDate())))
                .toList();
    }

    private MedicineResponse toResponse(Medicine medicine) {
        List<StockBatch> batches = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine);
        int totalStock = batches.stream().mapToInt(StockBatch::getQuantity).sum();
        var earliestExpiry = batches.isEmpty() ? null : batches.get(0).getExpiryDate();
        String status = totalStock <= medicine.getMinStock() ? "Low Stock" : "In Stock";
        List<StockBatchResponse> batchResponses = batches.stream()
                .map(b -> new StockBatchResponse(b.getId(), b.getBatchNumber(), b.getExpiryDate(),
                        b.getQuantity(), b.getUnitCostPrice(), b.getReceivedAt()))
                .toList();
        return new MedicineResponse(
                medicine.getId(),
                medicine.getName(),
                medicine.getCategory(),
                medicine.getManufacturer(),
                totalStock,
                medicine.getMinStock(),
                medicine.getPrice(),
                earliestExpiry,
                status,
                batchResponses);
    }
}
