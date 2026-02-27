package com.pharmacy.controller.web;

import com.pharmacy.dto.SettingsResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsWebController {

    private final SettingsService settingsService;

    @GetMapping
    public String showSettings(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        SettingsResponse settings = settingsService.getSettings(principal);
        model.addAttribute("settings", settings);
        return "settings";
    }
}
