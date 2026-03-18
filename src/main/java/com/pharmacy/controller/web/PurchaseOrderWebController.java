package com.pharmacy.controller.web;

import com.pharmacy.dto.PurchaseOrderRequest;
import com.pharmacy.dto.PurchaseOrderPaymentRequest;
import com.pharmacy.dto.PurchaseOrderResponse;
import com.pharmacy.dto.ReceivePurchaseOrderRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PurchaseOrderService;
import jakarta.validation.Valid;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderWebController {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    @GetMapping("/overview")
    public String overview(@AuthenticationPrincipal AppUserPrincipal principal,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "25") int size,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String query,
                           Model model) {
        Page<PurchaseOrderResponse> poPage =
            purchaseOrderService.listPaginated(principal, page, size, status, query);

        // Stats calculation using full list
        List<PurchaseOrderResponse> allOrders = purchaseOrderService.all(principal);
        long totalOrders = allOrders.size();
        long draftOrders = allOrders.stream().filter(po -> po.status().name().equals("DRAFT")).count();
        long orderedOrders = allOrders.stream().filter(po -> po.status().name().equals("ORDERED")).count();
        long partialReceivedOrders = allOrders.stream().filter(po -> po.status().name().equals("PARTIALLY_RECEIVED")).count();
        long receivedOrders = allOrders.stream().filter(po -> po.status().name().equals("RECEIVED")).count();

        BigDecimal totalValue = allOrders.stream()
                .map(PurchaseOrderResponse::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("draftOrders", draftOrders);
        model.addAttribute("orderedOrders", orderedOrders);
        model.addAttribute("partialReceivedOrders", partialReceivedOrders);
        model.addAttribute("receivedOrders", receivedOrders);
        model.addAttribute("totalValue", totalValue);
        
        int totalPages = poPage.getTotalPages();
        if (totalPages == 0) totalPages = 1;

        model.addAttribute("purchaseOrders", poPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", poPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("currentQuery", query);

        int windowStart, windowEnd;
        if (page < 3) {
            windowStart = 0;
            windowEnd = Math.min(page + 2, totalPages - 1);
        } else if (page >= totalPages - 3) {
            windowStart = Math.max(0, page - 2);
            windowEnd = totalPages - 1;
        } else {
            windowStart = page - 2;
            windowEnd = page + 2;
        }
        model.addAttribute("windowStart", windowStart);
        model.addAttribute("windowEnd", windowEnd);

        return "purchase-overview";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("suppliers", supplierService.list(principal));
        model.addAttribute("medicines", inventoryService.listMedicines(principal));
        return "purchase-order-form";
    }

    @GetMapping
    public String listPurchaseOrders(@AuthenticationPrincipal AppUserPrincipal principal,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "25") int size,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String query,
                                     Model model) {
        return overview(principal, page, size, status, query, model);
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

    @PostMapping("/{id}/cancel")
    public String cancel(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        purchaseOrderService.cancel(principal, id);
        return "redirect:/purchase-orders/" + id;
    }

    @PostMapping("/{id}/payments")
    public String addPayment(@AuthenticationPrincipal AppUserPrincipal principal,
                             @PathVariable Long id,
                             @Valid @ModelAttribute PurchaseOrderPaymentRequest request) {
        purchaseOrderService.addPayment(principal, id, request);
        return "redirect:/purchase-orders/" + id;
    }
}
