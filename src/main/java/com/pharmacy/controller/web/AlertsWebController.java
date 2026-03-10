package com.pharmacy.controller.web;

import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertsWebController {

    private final InventoryService inventoryService;

    @GetMapping({ "", "/expiry-alerts" })
    public String showAlerts(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        var expiryAlerts = inventoryService.getExpiryAlerts(principal, 30);
        model.addAttribute("expiryAlerts", expiryAlerts);
        model.addAttribute("activePage", "expiry-alerts");

        var medicines = inventoryService.listMedicines(principal);
        var lowStockCount = inventoryService.countBelowThresholdMedicines(principal);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("medicines", medicines);

        return "expiry-alerts";
    }

    @GetMapping("/low-stock")
    public String showLowStockAlerts(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "25") int size,
            Model model) {
        if (page < 1) page = 1;
        var alertsPage = inventoryService.getBelowThresholdMedicinesPaginated(principal, page - 1, size);
        
        int totalPages = alertsPage.getTotalPages();
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        model.addAttribute("lowStockMedicines", alertsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", alertsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        // Window logic for pagination
        int windowStart = Math.max(1, page - 2);
        int windowEnd = Math.min(totalPages, page + 2);
        if (windowEnd - windowStart < 4 && totalPages >= 5) {
            if (windowStart == 1) windowEnd = 5;
            else windowStart = totalPages - 4;
        }
        model.addAttribute("windowStart", windowStart);
        model.addAttribute("windowEnd", windowEnd);

        model.addAttribute("activePage", "low-stock-alerts");
        return "low-stock-alerts";
    }
}
