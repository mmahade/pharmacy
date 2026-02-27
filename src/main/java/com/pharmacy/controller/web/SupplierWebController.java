package com.pharmacy.controller.web;

import com.pharmacy.dto.SupplierResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        return "suppliers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        return "supplier-form";
    }
}
