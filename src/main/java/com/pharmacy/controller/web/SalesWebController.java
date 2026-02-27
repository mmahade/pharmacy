package com.pharmacy.controller.web;

import com.pharmacy.dto.SaleResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesWebController {

    private final SalesService salesService;

    @GetMapping
    public String listSales(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        List<SaleResponse> sales = salesService.listSales(principal);
        model.addAttribute("sales", sales);
        return "sales";
    }

    @GetMapping("/new")
    public String showNewSaleForm(Model model) {
        return "sale-form";
    }
}
