package com.pharmacy.controller;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "High-level summary statistics for the pharmacy dashboard")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Dashboard summary", description = "Returns KPI counts: total sales today, low-stock medicines, expiring batches, pending orders, etc.")
    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.summary(principal);
    }
}
