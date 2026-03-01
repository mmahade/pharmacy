package com.pharmacy.controller.web;

import com.pharmacy.dto.SaleResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import com.pharmacy.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesWebController {

    private final SalesService salesService;
    private final InventoryService inventoryService;

    @GetMapping
    public String listSales(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<SaleResponse> sales = salesService.listSales(principal);
        model.addAttribute("sales", sales);
        model.addAttribute("activePage", "sales");
        return "sales";
    }

    @GetMapping("/new")
    public String showNewSaleForm(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("medicines", inventoryService.listMedicines(principal));
        model.addAttribute("activePage", "pos");
        return "sale-form";
    }

    @GetMapping("/{id}")
    public String viewSaleDetails(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id,
            Model model) {
        SaleResponse sale = salesService.getSale(principal, id);
        model.addAttribute("sale", sale);
        model.addAttribute("activePage", "sales");
        return "sale-details";
    }
}
