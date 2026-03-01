package com.pharmacy.controller.web;

import com.pharmacy.dto.PurchaseOrderRequest;
import com.pharmacy.dto.PurchaseOrderResponse;
import com.pharmacy.dto.ReceivePurchaseOrderRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PurchaseOrderService;
import jakarta.validation.Valid;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderWebController {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    @GetMapping("/overview")
    public String overview(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<PurchaseOrderResponse> purchaseOrders = purchaseOrderService.list(principal);

        long totalOrders = purchaseOrders.size();
        long pendingOrders = purchaseOrders.stream().filter(po -> po.status().name().equals("SUBMITTED")).count();
        long receivedOrders = purchaseOrders.stream().filter(po -> po.status().name().equals("RECEIVED")).count();
        java.math.BigDecimal totalValue = purchaseOrders.stream()
                .map(PurchaseOrderResponse::totalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("receivedOrders", receivedOrders);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("recentOrders", purchaseOrders.stream().limit(5).toList());

        return "purchase-overview";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("suppliers", supplierService.list(principal));
        model.addAttribute("medicines", inventoryService.listMedicines(principal));
        return "purchase-order-form";
    }

    @GetMapping
    public String listPurchaseOrders(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<PurchaseOrderResponse> purchaseOrders = purchaseOrderService.list(principal);
        model.addAttribute("purchaseOrders", purchaseOrders);
        return "purchase-orders";
    }

    @GetMapping("/{id}")
    public String get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id, Model model) {
        PurchaseOrderResponse purchaseOrder = purchaseOrderService.get(principal, id);
        model.addAttribute("purchaseOrder", purchaseOrder);
        return "purchase-order-details";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @ModelAttribute PurchaseOrderRequest request) {
        purchaseOrderService.create(principal, request);
        return "redirect:/purchase-orders";
    }

    @PostMapping("/{id}/submit")
    public String submit(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        purchaseOrderService.submit(principal, id);
        return "redirect:/purchase-orders";
    }

    @PostMapping("/{id}/receive")
    public String receive(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            @Valid @ModelAttribute ReceivePurchaseOrderRequest request) {
        purchaseOrderService.receive(principal, id, request);
        return "redirect:/purchase-orders/" + id;
    }
}
