package com.pharmacy.service;

import com.pharmacy.dto.SaleReturnItemRequest;
import com.pharmacy.dto.SaleReturnRequest;
import com.pharmacy.dto.SaleReturnResponse;
import com.pharmacy.dto.SaleReturnItemResponse;
import com.pharmacy.entity.*;
import com.pharmacy.repository.*;
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

@Service
@RequiredArgsConstructor
public class SaleReturnService {

    private final SaleReturnRepository saleReturnRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TenantAccessService tenantAccessService;

    public List<SaleReturnResponse> list(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return saleReturnRepository.findBySale_PharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SaleReturnResponse get(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        SaleReturn sr = saleReturnRepository.findByIdAndSale_Pharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sales return not found"));
        return toResponse(sr);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public SaleReturnResponse create(AppUserPrincipal principal, SaleReturnRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);

        SaleTransaction sale = saleTransactionRepository.findByIdAndPharmacy(request.saleId(), pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale transaction not found"));

        SaleReturn sr = new SaleReturn();
        sr.setSale(sale);
        sr.setCreatedBy(currentUser);
        sr.setReturnNumber(generateReturnNumber(pharmacy.getName()));
        sr.setReturnDate(request.returnDate() == null ? LocalDate.now() : request.returnDate());
        sr.setReason(request.reason() != null ? request.reason().trim() : null);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleReturnItemRequest itemReq : request.items()) {
            SaleItem saleItem = sale.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.saleItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Item not found in this sale: " + itemReq.saleItemId()));

            // Check if already returned
            int alreadyReturned = sale.getReturns().stream()
                    .flatMap(r -> r.getItems().stream())
                    .filter(ri -> ri.getSaleItem().getId().equals(saleItem.getId()))
                    .mapToInt(SaleReturnItem::getQuantityReturned)
                    .sum();

            if (itemReq.quantityReturned() + alreadyReturned > saleItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Return quantity for " + saleItem.getMedicine().getName() + " exceeds original quantity");
            }

            SaleReturnItem sri = new SaleReturnItem();
            sri.setSaleReturn(sr);
            sri.setSaleItem(saleItem);
            sri.setQuantityReturned(itemReq.quantityReturned());
            sri.setUnitPrice(saleItem.getUnitPrice());
            sri.setLineTotal(saleItem.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.quantityReturned())));
            sr.getItems().add(sri);

            totalAmount = totalAmount.add(sri.getLineTotal());

            // Restock inventory
            restockInventory(saleItem, itemReq.quantityReturned());
        }

        sr.setTotalAmount(totalAmount);
        SaleReturn saved = saleReturnRepository.save(sr);
        return toResponse(saved);
    }

    private void restockInventory(SaleItem saleItem, int quantity) {
        int toReturn = quantity;
        for (SaleItemAllocation alloc : saleItem.getAllocations()) {
            if (toReturn <= 0)
                break;

            StockBatch batch = alloc.getStockBatch();
            // Since we validated total quantity in the caller,
            // we can safely restock into the original batch(es).
            batch.setQuantity(batch.getQuantity() + toReturn);
            stockBatchRepository.save(batch);

            // For now we assume the return fits back into the original batch flow.
            // If it came from multiple batches, we just put it back.
            toReturn = 0;
        }
    }

    private String generateReturnNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (code.length() > 4)
            code = code.substring(0, 4);
        return "SR-" + code + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private SaleReturnResponse toResponse(SaleReturn sr) {
        List<SaleReturnItemResponse> items = sr.getItems().stream()
                .map(i -> new SaleReturnItemResponse(
                        i.getId(),
                        i.getSaleItem().getId(),
                        i.getSaleItem().getMedicine().getName(),
                        i.getQuantityReturned(),
                        i.getUnitPrice(),
                        i.getLineTotal()))
                .toList();

        return new SaleReturnResponse(
                sr.getId(),
                sr.getReturnNumber(),
                sr.getReturnDate(),
                sr.getTotalAmount(),
                sr.getReason(),
                sr.getCreatedAt(),
                sr.getSale().getId(),
                sr.getSale().getSaleNumber(),
                items);
    }
}
