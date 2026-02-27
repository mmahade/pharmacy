package com.pharmacy.controller.web;

import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionWebController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public String listPrescriptions(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<PrescriptionResponse> prescriptions = prescriptionService.listPrescriptions(principal);
        model.addAttribute("prescriptions", prescriptions);
        return "prescriptions";
    }

    @GetMapping("/new")
    public String showNewPrescriptionForm(Model model) {
        return "prescription-form";
    }
}
