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
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertsWebController {

    private final InventoryService inventoryService;

    @GetMapping({ "", "/expiry-alerts" })
    public String showAlerts(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var expiryAlerts = inventoryService.getExpiryAlerts(principal, 30);
        model.addAttribute("expiryAlerts", expiryAlerts);

        var medicines = inventoryService.listMedicines(principal);
        var lowStockCount = medicines.stream().filter(m -> m.totalStock() <= m.minStock()).count();
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("medicines", medicines);

        return "expiry-alerts";
    }

    @GetMapping("/low-stock")
    public String showLowStockAlerts(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var medicines = inventoryService.listMedicines(principal);
        var lowStockMedicines = medicines.stream()
                .filter(m -> m.totalStock() <= m.minStock())
                .toList();
        model.addAttribute("lowStockMedicines", lowStockMedicines);
        return "low-stock-alerts";
    }
}
