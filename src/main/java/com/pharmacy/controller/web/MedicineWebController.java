package com.pharmacy.controller.web;

import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "9") int size,
            Model model,
            HttpServletRequest request) {

        org.springframework.data.domain.Page<MedicineResponse> medicinePage;
        if (query != null && !query.isEmpty()) {
            medicinePage = inventoryService.searchMedicinesPaginated(principal, query, page, size);
            model.addAttribute("searchQuery", query);
        } else {
            medicinePage = inventoryService.listMedicinesPaginated(principal, page, size);
        }

        int totalPages = medicinePage.getTotalPages();
        if (totalPages == 0) totalPages = 1;

        model.addAttribute("medicines", medicinePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", medicinePage.getTotalElements());
        model.addAttribute("pageSize", size);

        int windowStart, windowEnd;
        if (page < 3) {
            // Near start: expand forward — Page1→3, Page2→4, Page3→5 items
            windowStart = 0;
            windowEnd = Math.min(page + 2, totalPages - 1);
        } else if (page >= totalPages - 3) {
            // Near end: expand backward — symmetric to near-start
            windowStart = Math.max(0, page - 2);
            windowEnd = totalPages - 1;
        } else {
            // Middle: centered 5-page window
            windowStart = page - 2;
            windowEnd = page + 2;
        }

        model.addAttribute("windowStart", windowStart);
        model.addAttribute("windowEnd", windowEnd);

        // Add statistics
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("totalMedicines", stats.totalMedicines());
        model.addAttribute("totalStockUnits", stats.totalStockUnits());
        model.addAttribute("totalValue", stats.totalValue());

        // Add alerts and detailed stock info
        model.addAttribute("lowStockCount", inventoryService.countLowStockMedicines(principal));
        model.addAttribute("outOfStockCount", inventoryService.countOutOfStockMedicines(principal));
        model.addAttribute("lowStockMedicines", inventoryService.getLowStockMedicinesPaginated(principal, 0, 5).getContent());
        model.addAttribute("outOfStockMedicines", inventoryService.getOutOfStockMedicinesPaginated(principal, 0, 5).getContent());
        model.addAttribute("expiryAlerts", inventoryService.getExpiryAlerts(principal, 30));

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "medicines :: medicineList";
        }

        return "medicines";
    }

    @GetMapping("/add")
    public String showAddForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model,
            HttpServletRequest request) {
        model.addAttribute("requestURI", request.getRequestURI());

        // Add statistics for the navbar badge
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("lowStockCount", stats.lowStockCount());

        // Add isEdit flag to avoid template exceptions
        model.addAttribute("isEdit", false);

        return "medicine-form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {
        model.addAttribute("requestURI", request.getRequestURI());

        MedicineResponse medicine = inventoryService.getMedicine(principal, id);
        model.addAttribute("medicine", medicine);
        model.addAttribute("isEdit", true);

        // Add statistics for the navbar badge
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("lowStockCount", stats.lowStockCount());

        return "medicine-form";
    }

    @GetMapping("/{id}")
    public String getMedicine(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {
        model.addAttribute("requestURI", request.getRequestURI());

        MedicineResponse medicine = inventoryService.getMedicine(principal, id);
        model.addAttribute("medicine", medicine);

        // Add statistics for the navbar badge
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("lowStockCount", stats.lowStockCount());

        return "medicine-detail";
    }

    @GetMapping("/master-search")
    public String masterSearch(@RequestParam(name = "q", defaultValue = "") String q, Model model) {
        if (q.length() >= 2) {
            var results = inventoryService.searchMasterMedicines(q);
            model.addAttribute("masterResults", results);
        } else {
            model.addAttribute("masterResults", List.of());
        }
        return "medicine-form :: masterSearchQueryResults";
    }
}
