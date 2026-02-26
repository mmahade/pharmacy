package com.pharmacy.controller;

import com.pharmacy.dto.PurchaseOrderRequest;
import com.pharmacy.dto.PurchaseOrderResponse;
import com.pharmacy.dto.ReceivePurchaseOrderRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Purchase Orders", description = "Supplier purchase orders — create, submit, and receive goods into stock")
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "List purchase orders", description = "Returns all purchase orders for the pharmacy.")
    @GetMapping
    public List<PurchaseOrderResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return purchaseOrderService.list(principal);
    }

    @Operation(summary = "Get purchase order", description = "Returns the full details of a single purchase order including its line items.")
    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return purchaseOrderService.get(principal, id);
    }

    @Operation(summary = "Create purchase order", description = "Creates a new draft purchase order for a supplier.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.create(principal, request);
    }

    @Operation(summary = "Submit purchase order", description = "Marks a draft purchase order as submitted/sent to supplier.")
    @PutMapping("/{id}/submit")
    public PurchaseOrderResponse submit(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return purchaseOrderService.submit(principal, id);
    }

    @Operation(summary = "Receive purchase order", description = "Records goods received against a submitted purchase order and updates stock batches.")
    @PutMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable UUID id,
                                         @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return purchaseOrderService.receive(principal, id, request);
    }
}
