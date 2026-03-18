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
            @RequestParam(defaultValue = "0") int page,
            Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        int pageSize = 10;
        org.springframework.data.domain.Page<PrescriptionResponse> prescriptionsPage;

        if (query != null && !query.isEmpty()) {
            prescriptionsPage = prescriptionService.searchPrescriptionsPaginated(principal, query, page, pageSize);
            model.addAttribute("searchQuery", query);
        } else {
            prescriptionsPage = prescriptionService.listPrescriptionsPaginated(principal, page, pageSize);
        }
        model.addAttribute("prescriptions", prescriptionsPage.getContent());
        model.addAttribute("currentPage", page);
        
        int totalPages = prescriptionsPage.getTotalPages();
        if (totalPages == 0) totalPages = 1;
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", prescriptionsPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);
        
        int windowStart = Math.max(0, page - 2);
        int windowEnd = Math.min(totalPages - 1, page + 2);
        if (windowEnd - windowStart < 4 && totalPages >= 5) {
            if (windowStart == 0) windowEnd = 4;
            else windowStart = totalPages - 5;
        }

        model.addAttribute("windowStart", windowStart);
        model.addAttribute("windowEnd", windowEnd);

        com.pharmacy.dto.PrescriptionStats stats = prescriptionService.getPrescriptionStats(principal);
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
