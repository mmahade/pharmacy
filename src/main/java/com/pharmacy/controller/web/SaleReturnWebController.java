package com.pharmacy.controller.web;

import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SaleReturnService;
import com.pharmacy.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sale-returns")
@RequiredArgsConstructor
public class SaleReturnWebController {

    private final SaleReturnService saleReturnService;
    private final SalesService salesService;

    @GetMapping
    public String list(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("returns", saleReturnService.list(principal));
        return "sale-returns";
    }

    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) Long saleId,
            Model model) {
        if (saleId != null) {
            model.addAttribute("sale", salesService.getSale(principal, saleId));
        }
        model.addAttribute("sales", salesService.listSales(principal));
        return "sale-return-form";
    }

    @GetMapping("/{id}")
    public String showDetails(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            Model model) {
        model.addAttribute("returnRecord", saleReturnService.get(principal, id));
        return "sale-return-details";
    }
}
