package com.pharmacy.controller.web;

import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.entity.PrescriptionStatus;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
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
            Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        List<PrescriptionResponse> prescriptions;
        if (query != null && !query.isEmpty()) {
            prescriptions = prescriptionService.searchPrescriptions(principal, query);
            model.addAttribute("searchQuery", query);
        } else {
            prescriptions = prescriptionService.listPrescriptions(principal);
        }
        model.addAttribute("prescriptions", prescriptions);

        long totalCount = prescriptions.size();
        long pendingCount = prescriptions.stream().filter(p -> p.status() == PrescriptionStatus.PENDING).count();
        long completedCount = prescriptions.stream().filter(p -> p.status() == PrescriptionStatus.COMPLETED).count();
        BigDecimal totalValue = prescriptions.stream()
                .map(PrescriptionResponse::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalValue", totalValue);
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
