package com.pharmacy.service;

import com.pharmacy.dto.PrescriptionItemRequest;
import com.pharmacy.dto.PrescriptionItemResponse;
import com.pharmacy.dto.PrescriptionRequest;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.entity.*;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PrescriptionRepository;
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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final SaleTransactionRepository saleTransactionRepository;
    private final TenantAccessService tenantAccessService;

    public List<PrescriptionResponse> listPrescriptions(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return prescriptionRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
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
        prescription.setPrescriptionNumber(nextPrescriptionNumber(pharmacy.getName()));
        prescription.setPatientName(request.patientName().trim());
        prescription.setDoctorName(request.doctorName().trim());
        prescription
                .setPrescriptionDate(request.prescriptionDate() == null ? LocalDate.now() : request.prescriptionDate());
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
        prescription.setTotalAmount(totalAmount);
        return toResponse(prescriptionRepository.save(prescription));
    }

    /**
     * Marks prescription as COMPLETED and deducts quantities from inventory (full
     * lifecycle).
     * Idempotent: if already COMPLETED, returns current state without deducting
     * again.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PrescriptionResponse completePrescription(AppUserPrincipal principal, Long prescriptionId) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);
        Prescription prescription = prescriptionRepository.findByIdAndPharmacy(prescriptionId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prescription not found"));

        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) {
            return toResponse(prescription);
        }

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
        prescription.setStatus(PrescriptionStatus.COMPLETED);
        prescriptionRepository.save(prescription);

        SaleTransaction sale = new SaleTransaction();
        sale.setPharmacy(pharmacy);
        sale.setCreatedBy(currentUser);
        sale.setPrescription(prescription);
        sale.setSaleNumber(nextSaleNumber(pharmacy.getName()));
        sale.setSaleDate(LocalDate.now());
        sale.setPaymentMethod(PaymentMethod.CASH);
        sale.setTotal(prescription.getTotalAmount());
        sale.setAmountPaid(prescription.getTotalAmount());
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
                if (remaining <= 0)
                    break;
                int take = Math.min(remaining, batch.getQuantity());
                if (take <= 0)
                    continue;
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
        SalePayment payment = new SalePayment();
        payment.setSale(sale);
        payment.setAmount(prescription.getTotalAmount());
        payment.setPaymentMethod(PaymentMethod.CASH);
        sale.getPayments().add(payment);
        saleTransactionRepository.save(sale);

        return toResponse(prescription);
    }

    private String nextSaleNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String pharmacyCode = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (pharmacyCode.length() > 4) {
            pharmacyCode = pharmacyCode.substring(0, 4);
        }
        return "S-" + pharmacyCode + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private String nextPrescriptionNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String pharmacyCode = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (pharmacyCode.length() > 4) {
            pharmacyCode = pharmacyCode.substring(0, 4);
        }
        return "RX-" + pharmacyCode + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
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
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPrescriptionNumber(),
                prescription.getPatientName(),
                prescription.getDoctorName(),
                prescription.getPrescriptionDate(),
                prescription.getStatus(),
                prescription.getTotalAmount(),
                items);
    }
}
