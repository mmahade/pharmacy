package com.pharmacy.service;

import com.pharmacy.dto.PrescriptionCompleteRequest;
import com.pharmacy.dto.PrescriptionItemRequest;
import com.pharmacy.dto.PrescriptionItemResponse;
import com.pharmacy.dto.PrescriptionRequest;
import com.pharmacy.dto.PrescriptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.pharmacy.dto.PrescriptionStats;
import com.pharmacy.dto.PrescriptionStatsResponse;
import com.pharmacy.entity.*;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PrescriptionRepository;
import com.pharmacy.repository.SaleTransactionRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final TenantAccessService tenantAccessService;

    public Page<PrescriptionResponse> listPrescriptionsPaginated(AppUserPrincipal principal, int page, int size) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        var pageable = PageRequest.of(page, size);
        return prescriptionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy, pageable)
                .map(this::toResponse);
    }

    public Page<PrescriptionResponse> searchPrescriptionsPaginated(AppUserPrincipal principal, String query, int page, int size) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        if (query == null || query.isBlank()) {
            return Page.empty();
        }
        return prescriptionRepository.searchByPharmacy(pharmacy, query, org.springframework.data.domain.PageRequest.of(page, size))
                .map(this::toResponse);
    }

    public PrescriptionStats getPrescriptionStats(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        long totalCount = prescriptionRepository.countByPharmacyId(pharmacy.getId());
        long pendingCount = prescriptionRepository.countByPharmacyAndStatus(pharmacy, PrescriptionStatus.PENDING);
        long completedCount = prescriptionRepository.countByPharmacyAndStatus(pharmacy, PrescriptionStatus.COMPLETED);
        BigDecimal totalValue = prescriptionRepository.sumTotalValueByPharmacy(pharmacy);
        return new PrescriptionStats(totalCount, pendingCount, completedCount, totalValue != null ? totalValue : BigDecimal.ZERO);
    }

    public List<PrescriptionResponse> listPrescriptions(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return prescriptionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PrescriptionResponse> searchPrescriptions(AppUserPrincipal principal, String query) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return prescriptionRepository.searchByPharmacy(pharmacy, query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PrescriptionResponse getPrescription(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Prescription prescription = prescriptionRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));
        return toResponse(prescription);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PrescriptionResponse createPrescription(AppUserPrincipal principal, PrescriptionRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);

        Prescription prescription = new Prescription();
        prescription.setPharmacy(pharmacy);
        prescription.setCreatedBy(currentUser);
        prescription.setPrescriptionNumber(nextPrescriptionNumber(pharmacy));
        prescription.setPatientName(request.patientName().trim());
        prescription.setDoctorName(request.doctorName().trim());
        prescription.setPrescriptionDate(request.prescriptionDate() == null ? LocalDate.now() : request.prescriptionDate());
        prescription.setStatus(request.status());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PrescriptionItemRequest itemReq : request.items()) {
            Medicine medicine = medicineRepository.findByIdAndPharmacy(itemReq.medicineId(), pharmacy)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Medicine not found: " + itemReq.medicineId()));

            BigDecimal unitPrice = medicine.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity()));
            totalAmount = totalAmount.add(lineTotal);

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicine(medicine);
            item.setQuantity(itemReq.quantity());
            item.setInstructions(itemReq.instructions() != null ? itemReq.instructions().trim() : null);
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            prescription.getItems().add(item);
        }
        BigDecimal discountAmount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal discountPercent = request.discountPercent() != null ? request.discountPercent() : BigDecimal.ZERO;
        BigDecimal subtotal = totalAmount;
        BigDecimal finalTotal = subtotal.subtract(discountAmount).max(BigDecimal.ZERO);

        prescription.setDiscountAmount(discountAmount);
        prescription.setDiscountPercent(discountPercent);
        prescription.setTotalAmount(finalTotal);
        Prescription saved = prescriptionRepository.save(prescription);

        if (saved.getStatus() == PrescriptionStatus.COMPLETED) {
            createSaleFromPrescription(principal, saved, 
                request.paymentMethod() != null ? request.paymentMethod() : PaymentMethod.CASH,
                request.amountPaid() != null ? request.amountPaid() : totalAmount,
                request.paymentReference(),
                request.dueDate());
        }

        return toResponse(saved);
    }

    /**
     * Marks prescription as COMPLETED and deducts quantities from inventory.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PrescriptionResponse completePrescription(AppUserPrincipal principal, Long prescriptionId, PrescriptionCompleteRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Prescription prescription = prescriptionRepository.findByIdAndPharmacy(prescriptionId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));

        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) {
            return toResponse(prescription);
        }

        // Validate stock for all items first
        for (PrescriptionItem item : prescription.getItems()) {
            Medicine medicine = item.getMedicine();
            int available = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine)
                    .stream().mapToInt(StockBatch::getQuantity).sum();
            if (available < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + medicine.getName() + ": required " + item.getQuantity()
                                + ", available " + available);
            }
        }

        // Apply discount if provided
        BigDecimal discountAmount = request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO;
        BigDecimal discountPercent = request.discountPercent() != null ? request.discountPercent() : BigDecimal.ZERO;
        BigDecimal originalTotal = prescription.getTotalAmount().add(prescription.getDiscountAmount()); // gross subtotal
        BigDecimal finalTotal = originalTotal.subtract(discountAmount).max(BigDecimal.ZERO);
        prescription.setDiscountAmount(discountAmount);
        prescription.setDiscountPercent(discountPercent);
        prescription.setTotalAmount(finalTotal);

        prescription.setStatus(PrescriptionStatus.COMPLETED);
        prescriptionRepository.save(prescription);

        createSaleFromPrescription(principal, prescription, request.paymentMethod(), request.amountPaid(), request.paymentReference(), request.dueDate());

        return toResponse(prescription);
    }

    private void createSaleFromPrescription(AppUserPrincipal principal, Prescription prescription, PaymentMethod method, BigDecimal amountPaid, String reference, LocalDate dueDate) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);

        SaleTransaction sale = new SaleTransaction();
        sale.setPharmacy(pharmacy);
        sale.setCreatedBy(currentUser);
        sale.setPrescription(prescription);
        sale.setSaleNumber(nextSaleNumber(pharmacy));
        sale.setSaleDate(LocalDate.now());
        sale.setPaymentMethod(method);
        sale.setTotal(prescription.getTotalAmount());
        sale.setDiscountAmount(prescription.getDiscountAmount());
        sale.setDiscountPercent(prescription.getDiscountPercent());
        sale.setAmountPaid(amountPaid != null ? amountPaid : BigDecimal.ZERO);
        sale.setDueDate(dueDate);
        
        List<String> summaryParts = new ArrayList<>();
        for (PrescriptionItem pi : prescription.getItems()) {
            Medicine medicine = pi.getMedicine();
            List<StockBatch> batches = stockBatchRepository.findByMedicineOrderByExpiryDateAsc(medicine);
            int remaining = pi.getQuantity();
            
            SaleItem si = new SaleItem();
            si.setSale(sale);
            si.setMedicine(medicine);
            si.setQuantity(pi.getQuantity());
            si.setUnitPrice(pi.getUnitPrice());
            si.setLineTotal(pi.getLineTotal());
            
            for (StockBatch batch : batches) {
                if (remaining <= 0) break;
                int take = Math.min(remaining, batch.getQuantity());
                if (take <= 0) continue;
                
                batch.setQuantity(batch.getQuantity() - take);
                stockBatchRepository.save(batch);
                
                SaleItemAllocation alloc = new SaleItemAllocation();
                alloc.setSaleItem(si);
                alloc.setStockBatch(batch);
                alloc.setQuantity(take);
                si.getAllocations().add(alloc);
                remaining -= take;
            }
            sale.getItems().add(si);
            summaryParts.add(medicine.getName() + " x " + pi.getQuantity());
        }
        sale.setItemsSummary(String.join(", ", summaryParts));

        if (sale.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            SalePayment payment = new SalePayment();
            payment.setSale(sale);
            payment.setAmount(sale.getAmountPaid());
            payment.setPaymentMethod(method);
            payment.setReference(reference);
            sale.getPayments().add(payment);
        }
        
        saleTransactionRepository.save(sale);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PrescriptionResponse addPayment(AppUserPrincipal principal, Long prescriptionId, com.pharmacy.dto.SalePaymentRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        Prescription prescription = prescriptionRepository.findByIdAndPharmacy(prescriptionId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));

        SaleTransaction sale = saleTransactionRepository.findByPrescription(prescription)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale transaction not found for this prescription"));

        BigDecimal balanceDue = sale.getTotal().subtract(sale.getAmountPaid());
        if (request.amount().compareTo(balanceDue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount cannot exceed balance due: " + balanceDue);
        }

        SalePayment payment = new SalePayment();
        payment.setSale(sale);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        sale.getPayments().add(payment);

        sale.setAmountPaid(sale.getAmountPaid().add(request.amount()));
        saleTransactionRepository.save(sale);

        return toResponse(prescription);
    }

    private String nextSaleNumber(Pharmacy pharmacy) {
        long count = saleTransactionRepository.countByPharmacy(pharmacy);
        return String.valueOf(count + 1);
    }

    private String nextPrescriptionNumber(Pharmacy pharmacy) {
        long count = prescriptionRepository.countByPharmacyId(pharmacy.getId());
        return String.valueOf(count + 1);
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        List<PrescriptionItemResponse> items = prescription.getItems().stream()
                .map(pi -> new PrescriptionItemResponse(
                        pi.getMedicine().getId(),
                        pi.getMedicine().getName(),
                        pi.getQuantity(),
                        pi.getInstructions(),
                        pi.getUnitPrice(),
                        pi.getLineTotal()))
                .toList();
        BigDecimal amountPaid = saleTransactionRepository.findByPrescription(prescription)
                .map(SaleTransaction::getAmountPaid)
                .orElse(BigDecimal.ZERO);

        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPrescriptionNumber(),
                prescription.getPatientName(),
                prescription.getDoctorName(),
                prescription.getPrescriptionDate(),
                prescription.getStatus(),
                prescription.getTotalAmount(),
                amountPaid,
                prescription.getDiscountAmount(),
                prescription.getDiscountPercent(),
                items);
    }
}
