package com.pharmacy.controller.web;

import com.pharmacy.dto.SupplierRequest;
import com.pharmacy.dto.SupplierResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierWebController {

    private final SupplierService supplierService;

    @GetMapping
    public String listSuppliers(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<SupplierResponse> suppliers = supplierService.list(principal);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("activePage", "suppliers");

        if (!model.containsAttribute("supplierRequest")) {
            model.addAttribute("supplierRequest", new SupplierRequest("", "", "", "", ""));
        }

        return "suppliers";
    }

    @PostMapping
    public String createSupplier(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @ModelAttribute("supplierRequest") SupplierRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.supplierRequest",
                    bindingResult);
            redirectAttributes.addFlashAttribute("supplierRequest", request);
            redirectAttributes.addFlashAttribute("error", "Please fix the errors in the form.");
            return "redirect:/suppliers";
        }

        try {
            supplierService.create(principal, request);
            redirectAttributes.addFlashAttribute("success", "Supplier created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating supplier: " + e.getMessage());
        }

        return "redirect:/suppliers";
    }

    @PostMapping("/{id}")
    public String updateSupplier(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            @Valid @ModelAttribute("supplierRequest") SupplierRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid supplier data provided.");
            return "redirect:/suppliers";
        }

        try {
            supplierService.update(principal, id, request);
            redirectAttributes.addFlashAttribute("success", "Supplier updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating supplier: " + e.getMessage());
        }

        return "redirect:/suppliers";
    }
}
