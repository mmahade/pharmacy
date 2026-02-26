package com.pharmacy.service;

import com.pharmacy.dto.*;
import com.pharmacy.entity.*;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.SaleReturnRepository;
import com.pharmacy.repository.SaleTransactionRepository;
import com.pharmacy.repository.StockBatchRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SaleTransactionRepository saleTransactionRepository;
    private final SaleReturnRepository saleReturnRepository;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TenantAccessService tenantAccessService;

    public List<SaleResponse> listSales(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return saleTransactionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SaleResponse getSale(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        SaleTransaction sale = saleTransactionRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));
        return toResponse(sale);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','STAFF')")
    public SaleResponse createSale(AppUserPrincipal principal, SaleRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);

        BigDecimal total = BigDecimal.ZERO;
        List<String> summaryParts = new ArrayList<>();

        SaleTransaction sale = new SaleTransaction();
        sale.setPharmacy(pharmacy);
        sale.setCreatedBy(currentUser);
        sale.setSaleNumber(nextSaleNumber(pharmacy.getName()));
        sale.setSaleDate(request.saleDate() == null ? LocalDate.now() : request.saleDate());
        sale.setPaymentMethod(request.paymentMethod());
        if (request.dueDate() != null) {
            sale.setDueDate(request.dueDate());
        }

        for (SaleItemRequest itemReq : request.items()) {
            Medicine medicine = medicineRepository.findByIdAndPharmacy(itemReq.medicineId(), pharmacy)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Medicine not found: " + itemReq.medicineId()));
            List<StockBatch> batches = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine);
            int available = batches.stream().mapToInt(StockBatch::getQuantity).sum();
            if (available < itemReq.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + medicine.getName() + ": required " + itemReq.quantity()
                                + ", available " + available);
            }

            BigDecimal unitPrice = medicine.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(lineTotal);

            SaleItem line = new SaleItem();
            line.setSale(sale);
            line.setMedicine(medicine);
            line.setQuantity(itemReq.quantity());
            line.setUnitPrice(unitPrice);
            line.setLineTotal(lineTotal);
            int remaining = itemReq.quantity();
            for (StockBatch batch : batches) {
                if (remaining <= 0)
                    break;
                int take = Math.min(remaining, batch.getQuantity());
                if (take <= 0)
                    continue;
                batch.setQuantity(batch.getQuantity() - take);
                stockBatchRepository.save(batch);
                SaleItemAllocation alloc = new SaleItemAllocation();
                alloc.setSaleItem(line);
                alloc.setStockBatch(batch);
                alloc.setQuantity(take);
                line.getAllocations().add(alloc);
                remaining -= take;
            }
            sale.getItems().add(line);
            summaryParts.add(medicine.getName() + " x " + itemReq.quantity());
        }

        sale.setTotal(total);
        sale.setItemsSummary(String.join(", ", summaryParts));

        BigDecimal initialPayment = request.initialPaymentAmount() != null
                && request.initialPaymentAmount().compareTo(BigDecimal.ZERO) > 0
                ? request.initialPaymentAmount()
                : BigDecimal.ZERO;
        if (initialPayment.compareTo(total) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial payment cannot exceed total");
        }
        sale.setAmountPaid(initialPayment);
        if (initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setAmount(initialPayment);
            payment.setPaymentMethod(
                    request.initialPaymentMethod() != null ? request.initialPaymentMethod() : request.paymentMethod());
            payment.setReference(request.initialPaymentReference());
            sale.getPayments().add(payment);
        }

        SaleTransaction saved = saleTransactionRepository.save(sale);
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','STAFF')")
    public SaleResponse addPayment(AppUserPrincipal principal, Long saleId, SalePaymentRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        SaleTransaction sale = saleTransactionRepository.findByIdAndPharmacy(saleId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));
        BigDecimal balanceDue = sale.getTotal().subtract(sale.getAmountPaid());
        if (request.amount().compareTo(balanceDue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount cannot exceed balance due " + balanceDue);
        }
        SalePayment payment = new SalePayment();
        payment.setSale(sale);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        sale.getPayments().add(payment);
        sale.setAmountPaid(sale.getAmountPaid().add(request.amount()));
        saleTransactionRepository.save(sale);
        return toResponse(sale);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','STAFF')")
    public SaleReturnResponse createReturn(AppUserPrincipal principal, Long saleId, SaleReturnRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);
        SaleTransaction sale = saleTransactionRepository.findByIdAndPharmacy(saleId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale not found"));

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setSale(sale);
        saleReturn.setCreatedBy(currentUser);
        saleReturn.setReturnNumber(nextReturnNumber(pharmacy.getName()));
        saleReturn.setReturnDate(request.returnDate() != null ? request.returnDate() : LocalDate.now());
        saleReturn.setReason(request.reason() != null ? request.reason().trim() : null);

        BigDecimal totalRefund = BigDecimal.ZERO;
        for (SaleReturnItemRequest itemReq : request.items()) {
            SaleItem saleItem = sale.getItems().stream()
                    .filter(i -> i.getId().equals(itemReq.saleItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Sale line not found: " + itemReq.saleItemId()));
            int alreadyReturned = sale.getReturns().stream()
                    .flatMap(r -> r.getItems().stream())
                    .filter(ri -> ri.getSaleItem().getId().equals(saleItem.getId()))
                    .mapToInt(SaleReturnItem::getQuantityReturned)
                    .sum();
            int maxReturnable = saleItem.getQuantity() - alreadyReturned;
            if (itemReq.quantityReturned() > maxReturnable) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot return " + itemReq.quantityReturned() + " of " + saleItem.getMedicine().getName()
                                + "; max returnable is " + maxReturnable);
            }
            BigDecimal lineTotal = saleItem.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.quantityReturned()));
            totalRefund = totalRefund.add(lineTotal);

            SaleReturnItem ri = new SaleReturnItem();
            ri.setSaleReturn(saleReturn);
            ri.setSaleItem(saleItem);
            ri.setQuantityReturned(itemReq.quantityReturned());
            ri.setUnitPrice(saleItem.getUnitPrice());
            ri.setLineTotal(lineTotal);
            saleReturn.getItems().add(ri);

            int toReturn = itemReq.quantityReturned();
            for (SaleItemAllocation alloc : saleItem.getAllocations()) {
                if (toReturn <= 0)
                    break;
                if (alloc.getQuantity() <= 0)
                    continue;
                int putBack = Math.min(toReturn, alloc.getQuantity());
                StockBatch batch = alloc.getStockBatch();
                batch.setQuantity(batch.getQuantity() + putBack);
                stockBatchRepository.save(batch);
                alloc.setQuantity(alloc.getQuantity() - putBack);
                toReturn -= putBack;
            }
            if (toReturn > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Return quantity exceeds allocated quantity for this line");
            }
        }
        saleReturn.setTotalAmount(totalRefund);
        saleReturnRepository.save(saleReturn);
        return toReturnResponse(saleReturn);
    }

    private String nextSaleNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String pharmacyCode = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (pharmacyCode.length() > 4)
            pharmacyCode = pharmacyCode.substring(0, 4);
        return "S-" + pharmacyCode + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private String nextReturnNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (code.length() > 4)
            code = code.substring(0, 4);
        return "SR-" + code + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private SaleResponse toResponse(SaleTransaction sale) {
        List<SaleItemResponse> items = sale.getItems().stream()
                .map(si -> new SaleItemResponse(
                        si.getMedicine().getId(),
                        si.getMedicine().getName(),
                        si.getQuantity(),
                        si.getUnitPrice(),
                        si.getLineTotal()))
                .toList();
        List<SalePaymentResponse> payments = sale.getPayments().stream()
                .map(p -> new SalePaymentResponse(p.getId(), p.getAmount(), p.getPaymentMethod(), p.getPaidAt(),
                        p.getReference()))
                .toList();
        List<SaleReturnResponse> returns = sale.getReturns().stream()
                .map(this::toReturnResponse)
                .toList();
        BigDecimal amountPaid = sale.getAmountPaid() != null ? sale.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal balanceDue = sale.getTotal().subtract(amountPaid);
        var rx = sale.getPrescription();
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getSaleNumber(),
                sale.getSaleDate(),
                sale.getItemsSummary(),
                items,
                sale.getPaymentMethod(),
                sale.getTotal(),
                amountPaid,
                balanceDue,
                sale.getDueDate(),
                payments,
                returns,
                rx != null ? rx.getId() : null,
                rx != null ? rx.getPrescriptionNumber() : null);
    }

    private SaleReturnResponse toReturnResponse(SaleReturn r) {
        List<SaleReturnItemResponse> items = r.getItems().stream()
                .map(i -> new SaleReturnItemResponse(
                        i.getId(),
                        i.getSaleItem().getId(),
                        i.getSaleItem().getMedicine().getName(),
                        i.getQuantityReturned(),
                        i.getUnitPrice(),
                        i.getLineTotal()))
                .toList();
        return new SaleReturnResponse(
                r.getId(),
                r.getReturnNumber(),
                r.getReturnDate(),
                r.getTotalAmount(),
                r.getReason(),
                r.getCreatedAt(),
                r.getSale().getId(),
                r.getSale().getSaleNumber(),
                items);
    }
}
