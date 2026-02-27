package com.pharmacy.controller.web;

import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineWebController {

    private final InventoryService inventoryService;

    @GetMapping
    public String listMedicines(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "q", required = false) String query,
            Model model) {
        List<MedicineResponse> medicines;
        if (query != null && !query.isEmpty()) {
            medicines = inventoryService.searchMedicines(principal, query);
            model.addAttribute("searchQuery", query);
        } else {
            medicines = inventoryService.listMedicines(principal);
        }

        model.addAttribute("medicines", medicines);

        // Add statistics
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("totalMedicines", stats.totalMedicines());
        model.addAttribute("lowStockCount", stats.lowStockCount());
        model.addAttribute("totalStockUnits", stats.totalStockUnits());
        model.addAttribute("totalValue", stats.totalValue());

        // Add alerts and detailed stock info
        model.addAttribute("expiryAlerts", inventoryService.getExpiryAlerts(principal, 30));
        model.addAttribute("lowStockMedicines", medicines.stream()
                .filter(m -> "Low Stock".equals(m.status()))
                .toList());

        return "medicines";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        // Form handling will be added in next step
        return "medicine-form";
    }
}
