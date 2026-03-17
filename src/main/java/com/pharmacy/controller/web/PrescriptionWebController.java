package com.pharmacy.controller.web;

import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.entity.PrescriptionStatus;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionWebController {

    private final PrescriptionService prescriptionService;
    private final InventoryService inventoryService;

    @GetMapping
    public String listPrescriptions(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model,
            HttpServletRequest request) {
        Page<PrescriptionResponse> prescriptionPage;
        if (query != null && !query.isEmpty()) {
            prescriptionPage = prescriptionService.searchPrescriptionsPaginated(principal, query, page, size);
            model.addAttribute("searchQuery", query);
        } else {
            prescriptionPage = prescriptionService.listPrescriptionsPaginated(principal, page, size);
        }
        model.addAttribute("prescriptions", prescriptionPage.getContent());

        int totalPages = prescriptionPage.getTotalPages();
        if (totalPages == 0) totalPages = 1;

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", prescriptionPage.getTotalElements());
        model.addAttribute("pageSize", size);

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

        var stats = prescriptionService.getPrescriptionStats(principal);
        model.addAttribute("totalCount", stats.totalCount());
        model.addAttribute("pendingCount", stats.pendingCount());
        model.addAttribute("completedCount", stats.completedCount());
        model.addAttribute("totalValue", stats.totalValue());
        
        model.addAttribute("activePage", "prescriptions");

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "prescriptions :: prescriptionList";
        }

        return "prescriptions";
    }

    @GetMapping("/new")
    public String showNewPrescriptionForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("medicines", inventoryService.listMedicines(principal));
        model.addAttribute("activePage", "prescriptions");
        return "prescription-form";
    }

    @GetMapping("/{id}")
    public String viewPrescription(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id, Model model) {
        model.addAttribute("prescription", prescriptionService.getPrescription(principal, id));
        model.addAttribute("activePage", "prescriptions");
        return "prescription-detail";
    }
}
