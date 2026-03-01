package com.pharmacy.controller.web;

import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryWebController {

    private final InventoryService inventoryService;

    @GetMapping
    public String listInventory(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        // Since InventoryService doesn't have a global "list all batches"
        // across all medicines in a single call (it's per medicine),
        // we might need to aggregate or just show medicines with their total stock.
        // For a dedicated "Inventory" page, showing medicines with low stock alerts
        // or a summary of batches is common.

        var medicines = inventoryService.listMedicines(principal);
        model.addAttribute("medicines", medicines);

        // Add some sample alerts for the demo
        var expiryAlerts = inventoryService.getExpiryAlerts(principal, 30);
        model.addAttribute("expiryAlerts", expiryAlerts);
        return "inventory";
    }

    @GetMapping("/stock-entry")
    public String showStockEntryForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var medicines = inventoryService.listMedicines(principal);
        model.addAttribute("medicines", medicines);
        return "stock-entry";
    }
}
