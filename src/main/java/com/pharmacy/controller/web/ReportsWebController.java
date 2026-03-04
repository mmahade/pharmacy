package com.pharmacy.controller.web;

import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportsWebController {

    private final ReportService reportService;

    @GetMapping
    public String showReports() {
        return "reports";
    }

    @GetMapping("/sales")
    public String salesReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getSalesReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/sale-returns")
    public String saleReturnReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getSaleReturnReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/purchases")
    public String purchaseReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getPurchaseReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/purchase-returns")
    public String purchaseReturnReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getPurchaseReturnReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/income")
    public String incomeReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getIncomeReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/expenses")
    public String expenseReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getExpenseReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/stock")
    public String stockReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getCurrentStockReport(principal));
        return "report-details";
    }

    @GetMapping("/customer-due")
    public String customerDueReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getCustomerDueReport(principal));
        return "report-details";
    }

    @GetMapping("/supplier-due")
    public String supplierDueReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getSupplierDueReport(principal));
        return "report-details";
    }

    @GetMapping("/profit-loss")
    public String profitLossReport(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            Model model) {
        if (start == null)
            start = LocalDate.now().minusDays(30);
        if (end == null)
            end = LocalDate.now();
        model.addAttribute("report", reportService.getProfitLossReport(principal, start, end));
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "report-details";
    }

    @GetMapping("/due-transactions")
    public String dueTransactionsReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getDueTransactionsReport(principal));
        return "report-details";
    }

    @GetMapping("/subscriptions")
    public String subscriptionReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getSubscriptionReport(principal));
        return "report-details";
    }

    @GetMapping("/expired")
    public String expiredReport(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        model.addAttribute("report", reportService.getExpiredProductReport(principal));
        return "report-details";
    }
}
