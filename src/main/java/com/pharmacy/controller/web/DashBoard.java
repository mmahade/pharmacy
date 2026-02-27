package com.pharmacy.controller.web;

import com.pharmacy.dto.DashboardSummaryResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashBoard {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        DashboardSummaryResponse summary = dashboardService.summary(principal);
        model.addAttribute("summary", summary);
        return "dashboard";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
