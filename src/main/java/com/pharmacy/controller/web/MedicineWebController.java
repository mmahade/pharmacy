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
            Model model,
            HttpServletRequest request) {
        List<MedicineResponse> medicines;
        if (query != null && !query.isEmpty()) {
            medicines = inventoryService.searchMedicines(principal, query);
            model.addAttribute("searchQuery", query);
        } else {
            medicines = inventoryService.listMedicines(principal);
        }

        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        int size = 9;
        int totalItems = medicines.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (totalPages == 0)
            totalPages = 1;
        if (page > totalPages)
            page = totalPages;
        if (page < 1)
            page = 1;

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);

        List<MedicineResponse> paginatedMedicines;
        if (fromIndex < totalItems && fromIndex >= 0) {
            paginatedMedicines = medicines.subList(fromIndex, toIndex);
        } else {
            paginatedMedicines = List.of();
        }

        model.addAttribute("medicines", paginatedMedicines);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);

        int windowStart = Math.max(1, page - 2);
        int windowEnd = Math.min(totalPages, page + 2);
        if (windowEnd - windowStart < 4 && totalPages >= 5) {
            if (windowStart == 1)
                windowEnd = 5;
            else
                windowStart = totalPages - 4;
        }

        model.addAttribute("windowStart", windowStart);
        model.addAttribute("windowEnd", windowEnd);

        // Add statistics
        var stats = inventoryService.getInventoryStats(principal);
        model.addAttribute("totalMedicines", stats.totalMedicines());
        model.addAttribute("lowStockCount", stats.lowStockCount());
        model.addAttribute("totalStockUnits", stats.totalStockUnits());
        model.addAttribute("totalValue", stats.totalValue());

        // Add alerts and detailed stock info
        model.addAttribute("expiryAlerts", inventoryService.getExpiryAlerts(principal, 30));
        model.addAttribute("lowStockMedicines", inventoryService.getLowStockMedicines(principal));

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
