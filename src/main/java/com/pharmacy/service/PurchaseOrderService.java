package com.pharmacy.service;

import com.pharmacy.dto.PurchaseOrderItemRequest;
import com.pharmacy.dto.PurchaseOrderRequest;
import com.pharmacy.dto.PurchaseOrderResponse;
import com.pharmacy.dto.ReceivePurchaseOrderRequest;
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
        return purchaseOrderRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy)
                .stream()
                .map(this::toResponse)
                .toList();
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
        po.setOrderNumber(nextOrderNumber(pharmacy.getName()));
        po.setOrderDate(request.orderDate() == null ? LocalDate.now() : request.orderDate());
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
                    : ("B-" + line.getMedicine().getName().replaceAll("[^A-Za-z0-9]", "").substring(0,
                    Math.min(10, line.getMedicine().getName().length())) + "-"
                    + System.currentTimeMillis() % 100000);
            line.setBatchNumber(batchNum);
            Medicine medicine = line.getMedicine();
            StockBatch batch = stockBatchRepository.findByMedicineAndBatchNumber(medicine, batchNum)
                    .orElseGet(() -> {
                        StockBatch b = new StockBatch();
                        b.setMedicine(medicine);
                        b.setBatchNumber(batchNum);
                        b.setExpiryDate(lineReq.expiryDate() != null ? lineReq.expiryDate()
                                : line.getPurchaseOrder().getOrderDate().plusYears(2));
                        b.setQuantity(0);
                        b.setUnitCostPrice(line.getUnitCostPrice());
                        return b;
                    });
            batch.setQuantity(batch.getQuantity() + lineReq.quantityReceived());
            if (line.getUnitCostPrice() != null)
                batch.setUnitCostPrice(line.getUnitCostPrice());
            stockBatchRepository.save(batch);
        }

        boolean allReceived = po.getItems().stream().allMatch(i -> i.getQuantityReceived() >= i.getQuantityOrdered());
        po.setStatus(allReceived ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        return toResponse(purchaseOrderRepository.save(po));
    }

    private String nextOrderNumber(String pharmacyName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = pharmacyName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (code.length() > 4)
            code = code.substring(0, 4);
        return "PO-" + code + "-" + suffix + "-" + System.currentTimeMillis() % 10000;
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        var items = po.getItems().stream()
                .map(i -> new com.pharmacy.dto.PurchaseOrderItemResponse(
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
                po.getCreatedAt(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                items);
    }
}
