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
        var medicines = inventoryService.listMedicines(principal);
        model.addAttribute("medicines", medicines);

        var expiryAlerts = inventoryService.getExpiryAlerts(principal, 30);
        model.addAttribute("expiryAlerts", expiryAlerts);

        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("totalMedicines", stats.totalMedicines());
        model.addAttribute("lowStockCount", stats.lowStockCount());
        model.addAttribute("totalStockUnits", stats.totalStockUnits());
        model.addAttribute("totalValue", stats.totalValue());

        var lowStockMedicines = inventoryService.getBelowThresholdMedicinesPaginated(principal, 0, 100).getContent();
        model.addAttribute("lowStockMedicines", lowStockMedicines);

        model.addAttribute("activePage", "inventory");
        return "inventory";
    }

    @GetMapping("/stock-entry")
    public String showStockEntryForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var medicines = inventoryService.listMedicines(principal);
        model.addAttribute("medicines", medicines);
        return "stock-entry";
    }
}
