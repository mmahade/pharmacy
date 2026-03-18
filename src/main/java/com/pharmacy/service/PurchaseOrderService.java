package com.pharmacy.service;

import com.pharmacy.dto.*;
import com.pharmacy.entity.*;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.PurchaseOrderRepository;
import com.pharmacy.repository.StockBatchRepository;
import com.pharmacy.repository.SupplierRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TenantAccessService tenantAccessService;

    public List<PurchaseOrderResponse> list(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return purchaseOrderRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy, PageRequest.of(0, 25))
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PurchaseOrderResponse> all(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        return purchaseOrderRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PurchaseOrderResponse> listPaginated(AppUserPrincipal principal) {
        return list(principal);
    }

    public Page<PurchaseOrderResponse> listPaginated(AppUserPrincipal principal, int page, int size, String status, String query) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrderStatus statusEnum = null;
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            try {
                statusEnum = PurchaseOrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        String searchPattern = (query != null && !query.isBlank()) ? "%" + query.trim().toLowerCase() + "%" : null;
        Page<PurchaseOrder> poPage = purchaseOrderRepository.search(pharmacy, statusEnum, searchPattern, pageable);
        return poPage.map(this::toResponse);
    }

    public PurchaseOrderResponse get(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
        return toResponse(po);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseOrderResponse create(AppUserPrincipal principal, PurchaseOrderRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount currentUser = tenantAccessService.currentUser(principal);
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .filter(s -> s.getPharmacy().getId().equals(pharmacy.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setPharmacy(pharmacy);
        po.setSupplier(supplier);
        po.setCreatedBy(currentUser);
        po.setOrderNumber(nextOrderNumber(pharmacy));
        po.setOrderDate(request.orderDate() == null ? LocalDate.now() : request.orderDate());
        po.setNotes(request.notes());
        po.setStatus(PurchaseOrderStatus.DRAFT);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemRequest itemReq : request.items()) {
            Medicine medicine = medicineRepository.findByIdAndPharmacy(itemReq.medicineId(), pharmacy)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Medicine not found: " + itemReq.medicineId()));
            BigDecimal lineTotal = itemReq.unitCostPrice().multiply(BigDecimal.valueOf(itemReq.quantityOrdered()));
            total = total.add(lineTotal);

            PurchaseOrderItem line = new PurchaseOrderItem();
            line.setPurchaseOrder(po);
            line.setMedicine(medicine);
            line.setQuantityOrdered(itemReq.quantityOrdered());
            line.setQuantityReceived(0);
            line.setUnitCostPrice(itemReq.unitCostPrice());
            line.setLineTotal(lineTotal);
            po.getItems().add(line);
        }
        po.setTotalAmount(total);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseOrderResponse submit(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DRAFT orders can be submitted");
        }
        po.setStatus(PurchaseOrderStatus.ORDERED);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseOrderResponse cancel(AppUserPrincipal principal, Long id) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));

        if (po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            return toResponse(po);
        }
        if (po.getStatus() != PurchaseOrderStatus.DRAFT && po.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only DRAFT or ORDERED purchase orders can be cancelled");
        }

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseOrderResponse addPayment(AppUserPrincipal principal, Long id, PurchaseOrderPaymentRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndPharmacy(id, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));

        if (po.getStatus() == PurchaseOrderStatus.DRAFT || po.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot record payment for DRAFT or CANCELLED purchase orders");
        }

        BigDecimal balanceDue = po.getTotalAmount().subtract(po.getAmountPaid());
        if (request.amount().compareTo(balanceDue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount cannot exceed balance due " + balanceDue);
        }

        PurchasePayment payment = new PurchasePayment();
        payment.setPurchaseOrder(po);
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setReference(request.reference());
        po.getPayments().add(payment);
        po.setAmountPaid(po.getAmountPaid().add(request.amount()));

        return toResponse(purchaseOrderRepository.save(po));
    }

    /**
     * Receive (full or partial) goods against a purchase order. Increases inventory
     * for each line.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public PurchaseOrderResponse receive(AppUserPrincipal principal, Long purchaseOrderId,
            ReceivePurchaseOrderRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        PurchaseOrder po = purchaseOrderRepository.findByIdAndPharmacy(purchaseOrderId, pharmacy)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
        if (po.getStatus() != PurchaseOrderStatus.ORDERED && po.getStatus() != PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only receive against ORDERED or PARTIALLY_RECEIVED orders");
        }

        for (ReceivePurchaseOrderRequest.ReceivePurchaseOrderLineRequest lineReq : request.lines()) {
            // Skip lines where nothing is being received this time
            if (lineReq.quantityReceived() == 0) continue;

            PurchaseOrderItem line = po.getItems().stream()
                    .filter(i -> i.getId().equals(lineReq.purchaseOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "PO line not found: " + lineReq.purchaseOrderItemId()));
            int newReceived = line.getQuantityReceived() + lineReq.quantityReceived();
            if (newReceived > line.getQuantityOrdered()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Received quantity " + lineReq.quantityReceived() + " would exceed ordered "
                                + line.getQuantityOrdered() + " for " + line.getMedicine().getName());
            }
            line.setQuantityReceived(newReceived);

            String batchNum = lineReq.batchNumber() != null && !lineReq.batchNumber().isBlank()
                    ? lineReq.batchNumber().trim()
                    : null;

            if (batchNum == null) {
                String sanitizedName = line.getMedicine().getName().replaceAll("[^A-Za-z0-9]", "");
                if (sanitizedName.isEmpty()) {
                    sanitizedName = "MED";
                }
                int prefixLength = Math.min(10, sanitizedName.length());
                String prefix = sanitizedName.substring(0, prefixLength);
                batchNum = "B-" + prefix + "-" + (System.currentTimeMillis() % 100000);
            }
            line.setBatchNumber(batchNum);
            Medicine medicine = line.getMedicine();
            LocalDate targetExpiry = lineReq.expiryDate() != null ? lineReq.expiryDate()
                    : line.getPurchaseOrder().getOrderDate().plusYears(2);

            String finalBatchNum = batchNum;
            StockBatch batch = stockBatchRepository.findByMedicineAndExpiryDate(medicine, targetExpiry)
                    .orElseGet(() -> stockBatchRepository.findByMedicineAndBatchNumber(medicine, finalBatchNum)
                            .orElseGet(() -> {
                                StockBatch b = new StockBatch();
                                b.setMedicine(medicine);
                                b.setBatchNumber(finalBatchNum);
                                b.setExpiryDate(targetExpiry);
                                b.setQuantity(0);
                                b.setUnitCostPrice(line.getUnitCostPrice());
                                return b;
                            }));

            batch.setQuantity(batch.getQuantity() + lineReq.quantityReceived());
            if (line.getUnitCostPrice() != null)
                batch.setUnitCostPrice(line.getUnitCostPrice());
            stockBatchRepository.save(batch);
        }
        // Process discount
        if (request.discountAmount() != null) {
            po.setDiscountAmount(request.discountAmount());
        }
        if (request.discountPercentage() != null) {
            po.setDiscountPercentage(request.discountPercentage());
        }

        // Process payments if provided during receiving
        BigDecimal totalPayment = request.totalPaymentAmount() != null ? request.totalPaymentAmount() : BigDecimal.ZERO;

        if (totalPayment.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balanceDue = po.getTotalAmount().subtract(po.getAmountPaid()).subtract(po.getDiscountAmount());
            if (totalPayment.compareTo(balanceDue) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Total payment amount " + totalPayment + " exceeds balance due " + balanceDue
                                + " (Total: " + po.getTotalAmount() + ", Paid: " + po.getAmountPaid()
                                + ", Discount: " + po.getDiscountAmount() + ")");
            }

            PurchasePayment payment = new PurchasePayment();
            payment.setPurchaseOrder(po);
            payment.setAmount(totalPayment);
            payment.setPaymentMethod(PaymentMethod.valueOf(
                    request.paymentMethod() != null ? request.paymentMethod() : "CASH"));
            payment.setReference(request.paymentReference());
            po.getPayments().add(payment);
            po.setAmountPaid(po.getAmountPaid().add(totalPayment));
        }

        // Determine final status:
        //   - If user explicitly chose "finalize / close order" → RECEIVED regardless of shortfall
        //   - Otherwise: RECEIVED only when every line is fully delivered
        boolean allReceived = po.getItems().stream().allMatch(i -> i.getQuantityReceived() >= i.getQuantityOrdered());
        boolean finalize = Boolean.TRUE.equals(request.finalizeOrder());
        po.setStatus((allReceived || finalize) ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        return toResponse(purchaseOrderRepository.save(po));
    }

    private String nextOrderNumber(Pharmacy pharmacy) {
        long count = purchaseOrderRepository.countByPharmacy(pharmacy);
        return String.valueOf(count + 1);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        var items = po.getItems().stream()
                .map(i -> new PurchaseOrderItemResponse(
                        i.getId(),
                        i.getMedicine().getId(),
                        i.getMedicine().getName(),
                        i.getQuantityOrdered(),
                        i.getQuantityReceived(),
                        i.getUnitCostPrice(),
                        i.getLineTotal(),
                        i.getBatchNumber()))
                .toList();
        return new PurchaseOrderResponse(
                po.getId(),
                po.getOrderNumber(),
                po.getOrderDate(),
                po.getStatus(),
                po.getTotalAmount(),
                po.getAmountPaid(),
                po.getCreatedAt(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getNotes(),

                items);
    }
}
