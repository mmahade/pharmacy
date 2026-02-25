package com.pharmacy.controller;

import com.pharmacy.dto.PurchaseOrderRequest;
import com.pharmacy.dto.PurchaseOrderResponse;
import com.pharmacy.dto.ReceivePurchaseOrderRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PurchaseOrderService;
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

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public List<PurchaseOrderResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return purchaseOrderService.list(principal);
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return purchaseOrderService.get(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                        @Valid @RequestBody PurchaseOrderRequest request) {
        return purchaseOrderService.create(principal, request);
    }

    @PutMapping("/{id}/submit")
    public PurchaseOrderResponse submit(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return purchaseOrderService.submit(principal, id);
    }

    @PutMapping("/{id}/receive")
    public PurchaseOrderResponse receive(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable UUID id,
                                         @Valid @RequestBody ReceivePurchaseOrderRequest request) {
        return purchaseOrderService.receive(principal, id, request);
    }
}
