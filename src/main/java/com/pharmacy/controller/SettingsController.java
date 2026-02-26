package com.pharmacy.controller;

import com.pharmacy.dto.PharmacySettingsUpdateRequest;
import com.pharmacy.dto.SettingsResponse;
import com.pharmacy.dto.UserProfileUpdateRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public SettingsResponse get(@AuthenticationPrincipal AppUserPrincipal principal) {
        return settingsService.getSettings(principal);
    }

    @PutMapping("/pharmacy")
    public SettingsResponse updatePharmacy(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @Valid @RequestBody PharmacySettingsUpdateRequest request) {
        return settingsService.updatePharmacy(principal, request);
    }

    @PutMapping("/profile")
    public SettingsResponse updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @Valid @RequestBody UserProfileUpdateRequest request) {
        return settingsService.updateProfile(principal, request);
    }
}
