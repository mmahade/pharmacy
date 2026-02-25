package com.pharmacy.controller;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.summary(principal);
    }
}
