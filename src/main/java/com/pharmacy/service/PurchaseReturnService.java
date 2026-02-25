package com.pharmacy.service;

import com.pharmacy.dto.PurchaseReturnItemRequest;
import com.pharmacy.dto.PurchaseReturnRequest;
import com.pharmacy.dto.PurchaseReturnResponse;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.PurchaseReturn;
import com.pharmacy.entity.PurchaseReturnItem;
import com.pharmacy.entity.StockBatch;
import com.pharmacy.entity.Supplier;
import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PurchaseReturnRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.repository.SupplierRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TenantAccessService tenantAccessService;

    public List<PurchaseReturnResponse> list(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return purchaseReturnRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PurchaseReturnResponse get(AppUserPrincipal principal, UUID id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseReturn pr = purchaseReturnRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase return not found"));
        return toResponse(pr);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseReturnResponse create(AppUserPrincipal principal, PurchaseReturnRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .filter(s -> s.getPharmacy().getId().equals(pharmacy.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        PurchaseReturn pr = new PurchaseReturn();
        pr.setPharmacy(pharmacy);
        pr.setSupplier(supplier);
        pr.setCreatedBy(currentUser);
        pr.setReturnNumber(nextReturnNumber(pharmacy.getName()));
        pr.setReturnDate(request.returnDate() == null ? LocalDate.now() : request.returnDate());
        pr.setReason(request.reason() != null ? request.reason().trim() : null);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseReturnItemRequest itemReq : request.items()) {
            Medicine medicine = medicineRepository.findByIdAndPharmacy(itemReq.medicineId(), pharmacy)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medicine not found: " + itemReq.medicineId()));
            List<StockBatch> batches = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine);
            int available = batches.stream().mapToInt(StockBatch::getQuantity).sum();
            if (available < itemReq.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + medicine.getName() + ": required " + itemReq.quantity() + ", available " + available);
            }
            BigDecimal lineTotal = itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(lineTotal);

            PurchaseReturnItem line = new PurchaseReturnItem();
            line.setPurchaseReturn(pr);
            line.setMedicine(medicine);
            line.setQuantity(itemReq.quantity());
            line.setUnitPrice(itemReq.unitPrice());
            line.setLineTotal(lineTotal);
            pr.getItems().add(line);

            int remaining = itemReq.quantity();
            for (StockBatch batch : batches) {
                if (remaining <= 0) break;
                int take = Math.min(remaining, batch.getQuantity());
                if (take <= 0) continue;
                batch.setQuantity(batch.getQuantity() - take);
                stockBatchRepository.save(batch);
                remaining -= take;
            }
        }
        pr.setTotalAmount(total);
        PurchaseReturn saved = purchaseReturnRepository.save(pr);
        return toResponse(saved);
    }

    private String nextReturnNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (code.length() > 4) code = code.substring(0, 4);
        return "PR-" + code + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private PurchaseReturnResponse toResponse(PurchaseReturn pr) {
        var items = pr.getItems().stream()
                .map(i -> new com.pharmacy.dto.PurchaseReturnItemResponse(
                        i.getId(),
                        i.getMedicine().getId(),
                        i.getMedicine().getName(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getLineTotal()
                ))
                .toList();
        return new PurchaseReturnResponse(
                pr.getId(),
                pr.getReturnNumber(),
                pr.getReturnDate(),
                pr.getTotalAmount(),
                pr.getReason(),
                pr.getCreatedAt(),
                pr.getSupplier().getId(),
                pr.getSupplier().getName(),
                pr.getPurchaseOrder() != null ? pr.getPurchaseOrder().getId() : null,
                items
        );
    }
}
