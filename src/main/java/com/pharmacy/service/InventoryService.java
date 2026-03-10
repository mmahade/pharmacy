package com.pharmacy.service;

import com.pharmacy.dto.*;
import com.pharmacy.entity.BdMedicine;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.repository.BdMedicineRepository;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InventoryService {

        private final MedicineRepository medicineRepository;
        private final StockBatchRepository stockBatchRepository;
        private final BdMedicineRepository bdMedicineRepository;
        private final TenantAccessService tenantAccessService;

        public List<MedicineResponse> listMedicines(AppUserPrincipal principal) {
                // Fetch first 500 as a reasonable default for legacy non-paginated callers
                return listMedicinesPaginated(principal, 0, 500).getContent();
        }

        public org.springframework.data.domain.Page<MedicineResponse> listMedicinesPaginated(AppUserPrincipal principal, int page, int size) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                var pageable = PageRequest.of(page, size);
                return medicineRepository.findByPharmacyId(pharmacy.getId(), pageable)
                                .map(this::toResponse);
        }

        public org.springframework.data.domain.Page<MedicineResponse> searchMedicinesPaginated(AppUserPrincipal principal, String query, int page, int size) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                if (query == null || query.isBlank()) {
                        return org.springframework.data.domain.Page.empty();
                }
                var pageable = PageRequest.of(page, size);
                return medicineRepository.searchMedicines(pharmacy, query.trim(), pageable)
                                .map(this::toResponse);
        }

        public List<MedicineResponse> searchMedicines(AppUserPrincipal principal, String query) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                if (query == null || query.isBlank()) {
                        return List.of();
                }
                var page = PageRequest.of(0, 100); // Increased limit slightly for legacy callers if any
                return medicineRepository.searchMedicines(
                                pharmacy, query.trim(), page)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public List<BdMedicineResponse> searchMasterMedicines(String query) {
                if (query == null || query.isBlank()) {
                        return List.of();
                }
                var page = PageRequest.of(0, 25);
                return bdMedicineRepository.searchMedicines(query.trim(), page)
                                .stream()
                                .map(m -> new BdMedicineResponse(
                                                m.getId(), m.getName(), m.getGenericName(),
                                                m.getCategory(), m.getManufacturer(),
                                                m.getDosageForm(), m.getStrength(), m.getDescription()))
                                .toList();
        }

        @Transactional
        @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
        public MedicineResponse createMedicine(AppUserPrincipal principal, MedicineRequest request) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);

                // Find or create master BdMedicine record
                BdMedicine master = bdMedicineRepository
                                .findByNameIgnoreCaseAndManufacturerIgnoreCase(
                                                request.name().trim(), request.manufacturer().trim())
                                .orElseGet(() -> {
                                        BdMedicine bd = new BdMedicine();
                                        bd.setName(request.name().trim());
                                        bd.setCategory(request.category() != null ? request.category().trim() : "General");
                                        bd.setManufacturer(request.manufacturer().trim());
                                        bd.setGenericName(request.genericName());
                                        bd.setDosageForm(request.dosageForm());
                                        bd.setStrength(request.strength());
                                        bd.setDescription(request.description());
                                        return bdMedicineRepository.save(bd);
                                });

                Medicine medicine = new Medicine();
                medicine.setPharmacy(pharmacy);
                medicine.setMasterMedicine(master);
                medicine.setMinStock(request.minStock());
                medicine.setPrice(request.price());
                return toResponse(medicineRepository.save(medicine));
        }

        public MedicineResponse getMedicine(AppUserPrincipal principal, Long id) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                Medicine medicine = medicineRepository.findByIdAndPharmacy(id, pharmacy)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Medicine not found"));
                return toResponse(medicine);
        }

        @Transactional
        @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
        public MedicineResponse updateMedicine(AppUserPrincipal principal, Long id, MedicineRequest request) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                Medicine medicine = medicineRepository.findByIdAndPharmacy(id, pharmacy)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Medicine not found"));

                // Update master catalog details on the linked BdMedicine
                BdMedicine master = medicine.getMasterMedicine();
                master.setName(request.name().trim());
                master.setCategory(request.category() != null ? request.category().trim() : master.getCategory());
                master.setManufacturer(request.manufacturer().trim());
                master.setGenericName(request.genericName());
                master.setDosageForm(request.dosageForm());
                master.setStrength(request.strength());
                master.setDescription(request.description());
                bdMedicineRepository.save(master);

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

                String providedBatchNumber = request.batchNumber() != null ? request.batchNumber().trim() : "";

                // Logic: 1. Try to find by Medicine + Expiry Date (Consolidation prioritized by
                // USER)
                // 2. Fallback to Medicine + Batch Number (if provided)
                // 3. Create New (Generate Batch Number if needed)
                StockBatch batch = stockBatchRepository
                                .findByMedicineAndExpiryDate(medicine, request.expiryDate())
                                .orElseGet(() -> {
                                        if (!providedBatchNumber.isBlank()) {
                                                return stockBatchRepository
                                                                .findByMedicineAndBatchNumber(medicine,
                                                                                providedBatchNumber)
                                                                .orElse(null);
                                        }
                                        return null;
                                });

                if (batch == null) {
                        batch = new StockBatch();
                        batch.setMedicine(medicine);
                        batch.setExpiryDate(request.expiryDate());
                        batch.setQuantity(0);

                        String finalBatchNum = providedBatchNumber;
                        if (finalBatchNum.isBlank()) {
                                // Serial number generation: Find last batch and increment
                                int nextSerial = stockBatchRepository.findFirstByMedicineOrderByIdDesc(medicine)
                                                .map(lastBatch -> {
                                                        try {
                                                                return Integer.parseInt(lastBatch.getBatchNumber()) + 1;
                                                        } catch (NumberFormatException e) {
                                                                return 1; // Fallback if last batch wasn't numeric
                                                        }
                                                })
                                                .orElse(1); // Start from 1 for new medicines

                                // Double check if this number already exists (human safety)
                                while (stockBatchRepository
                                                .findByMedicineAndBatchNumber(medicine, String.valueOf(nextSerial))
                                                .isPresent()) {
                                        nextSerial++;
                                }
                                finalBatchNum = String.valueOf(nextSerial);
                        }
                        batch.setBatchNumber(finalBatchNum);
                }

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

        /**
         * Counts all medicines that are in stock.
         */
        public long countInStockMedicines(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                return medicineRepository.countInStockMedicines(pharmacy)
                        .stream().filter(Objects::nonNull).mapToLong(Long::longValue).sum();
        }

        /**
         * Counts all medicines that are low stock.
         */
        public long countLowStockMedicines(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                return medicineRepository.countLowStockMedicines(pharmacy)
                        .stream().filter(Objects::nonNull).mapToLong(Long::longValue).sum();
        }

        /**
         * Counts all medicines that are out of stock.
         */
        public long countOutOfStockMedicines(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                return medicineRepository.countOutOfStockMedicines(pharmacy)
                        .stream().filter(Objects::nonNull).mapToLong(Long::longValue).sum();
        }

        /**
         * Get paginated medicines that are low stock.
         */
        public List<MedicineResponse> getLowStockMedicinesPaginated(AppUserPrincipal principal, int page, int size) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                return medicineRepository.findLowStockMedicines(pharmacy, PageRequest.of(page, size))
                        .stream()
                        .map(this::toResponse)
                        .toList();
        }

        /**
         * Get all medicines that are low stock.
         * Deprecated for large datasets, use paginated version.
         */
        @Deprecated
        public List<MedicineResponse> getLowStockMedicines(AppUserPrincipal principal) {
                return getLowStockMedicinesPaginated(principal, 0, 10);
        }

        /**
         * Get paginated medicines that are out of stock.
         */
        public List<MedicineResponse> getOutOfStockMedicinesPaginated(AppUserPrincipal principal, int page, int size) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
                return medicineRepository.findOutOfStockMedicines(pharmacy, PageRequest.of(page, size))
                        .stream()
                        .map(this::toResponse)
                        .toList();
        }

        /**
         * Get all medicines that are out of stock.
         * Deprecated for large datasets.
         */
        @Deprecated
        public List<MedicineResponse> getOutOfStockMedicines(AppUserPrincipal principal) {
                return listMedicines(principal).stream()
                                .filter(m -> m.totalStock() <= 0)
                                .toList();
        }

        public InventoryStatsResponse getInventoryStats(AppUserPrincipal principal) {
                Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);

                long totalMedicines = medicineRepository.countByPharmacyId(pharmacy.getId());
                long lowStockCount = countLowStockMedicines(principal);

                List<Object[]> aggregation = medicineRepository.getInventoryAggregation(pharmacy);
                Object[] row = (aggregation != null && !aggregation.isEmpty()) ? aggregation.get(0) : new Object[] { 0L, java.math.BigDecimal.ZERO };

                // Handle the case where the query returns a single array or a list containing one array
                Long totalStockUnits = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                java.math.BigDecimal totalValue = row[1] != null ? (java.math.BigDecimal) row[1] : java.math.BigDecimal.ZERO;

                return new InventoryStatsResponse(totalMedicines, lowStockCount, totalStockUnits, totalValue);
        }

        private MedicineResponse toResponse(Medicine medicine) {
                List<StockBatch> batches = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine);
                int totalStock = batches.stream()
                                .filter(b -> b.getExpiryDate() != null && !b.getExpiryDate().isBefore(LocalDate.now()))
                                .mapToInt(StockBatch::getQuantity).sum();
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
                                medicine.getGenericName(),
                                medicine.getDosageForm(),
                                medicine.getStrength(),
                                medicine.getDescription(),
                                earliestExpiry,
                                status,
                                batchResponses);
        }
}
