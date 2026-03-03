package com.pharmacy.controller.web;

import com.pharmacy.dto.PurchaseReturnResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.PurchaseReturnService;
import com.pharmacy.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/purchase-returns")
@RequiredArgsConstructor
public class PurchaseReturnWebController {

    private final PurchaseReturnService purchaseReturnService;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<PurchaseReturnResponse> returns = purchaseReturnService.list(principal);
        model.addAttribute("purchaseReturns", returns);

        // Summary stats for the list view
        model.addAttribute("totalReturnsCount", returns.size());
        model.addAttribute("totalReturnsValue", returns.stream()
                .map(PurchaseReturnResponse::totalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        return "purchase-return";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("suppliers", supplierService.list(principal));
        model.addAttribute("medicines", inventoryService.listMedicines(principal));
        return "purchase-return-form";
    }

    @GetMapping("/{id}")
    public String getDetails(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id, Model model) {
        PurchaseReturnResponse purchaseReturn = purchaseReturnService.get(principal, id);
        model.addAttribute("purchaseReturn", purchaseReturn);
        return "purchase-return-details";
    }
}
