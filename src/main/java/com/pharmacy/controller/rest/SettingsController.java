package com.pharmacy.controller.rest;

import com.pharmacy.dto.PharmacySettingsUpdateRequest;
import com.pharmacy.dto.SettingsResponse;
import com.pharmacy.dto.UserProfileUpdateRequest;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Settings", description = "Pharmacy profile and user account settings")
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @Operation(summary = "Get settings", description = "Returns the combined pharmacy profile and current user profile settings.")
    @GetMapping
    public SettingsResponse get(@AuthenticationPrincipal AppUserPrincipal principal) {
        return settingsService.getSettings(principal);
    }

    @Operation(summary = "Update pharmacy profile", description = "Updates the pharmacy's name, address, and contact information.")
    @PutMapping("/pharmacy")
    public SettingsResponse updatePharmacy(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @Valid @RequestBody PharmacySettingsUpdateRequest request) {
        return settingsService.updatePharmacy(principal, request);
    }

    @Operation(summary = "Update user profile", description = "Updates the authenticated user's name and password.")
    @PutMapping("/profile")
    public SettingsResponse updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                                          @Valid @RequestBody UserProfileUpdateRequest request) {
        return settingsService.updateProfile(principal, request);
    }
}
