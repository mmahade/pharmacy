package com.pharmacy.service;

import com.pharmacy.dto.PharmacySettingsUpdateRequest;
import com.pharmacy.dto.SettingsResponse;
import com.pharmacy.dto.UserProfileUpdateRequest;
import com.pharmacy.entity.Pharmacy;
import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.UserAccountRepository;
import com.pharmacy.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final TenantAccessService tenantAccessService;
    private final UserAccountRepository userAccountRepository;

    public SettingsResponse getSettings(AppUserPrincipal principal) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount user = tenantAccessService.currentUser(principal);
        return toResponse(pharmacy, user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SettingsResponse updatePharmacy(AppUserPrincipal principal, PharmacySettingsUpdateRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount user = tenantAccessService.currentUser(principal);

        pharmacy.setName(request.name().trim());
        pharmacy.setLicenseNumber(request.licenseNumber().trim());
        pharmacy.setPhoneNumber(request.phoneNumber().trim());
        pharmacy.setEmail(request.email().trim().toLowerCase());
        pharmacy.setAddress(request.address().trim());

        return toResponse(pharmacy, user);
    }

    @Transactional
    public SettingsResponse updateProfile(AppUserPrincipal principal, UserProfileUpdateRequest request) {
        Pharmacy pharmacy = tenantAccessService.currentPharmacy(principal);
        UserAccount user = tenantAccessService.currentUser(principal);

        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhoneNumber(request.phoneNumber().trim());
        userAccountRepository.save(user);

        return toResponse(pharmacy, user);
    }

    private SettingsResponse toResponse(Pharmacy pharmacy, UserAccount user) {
        return new SettingsResponse(
                new SettingsResponse.PharmacySettings(
                        pharmacy.getId(),
                        pharmacy.getName(),
                        pharmacy.getLicenseNumber(),
                        pharmacy.getPhoneNumber(),
                        pharmacy.getEmail(),
                        pharmacy.getAddress()
                ),
                new SettingsResponse.UserProfile(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole()
                )
        );
    }
}
