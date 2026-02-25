package com.pharmacy.dto;

import com.pharmacy.entity.Role;

import java.util.UUID;

public record SettingsResponse(
        PharmacySettings pharmacy,
        UserProfile user
) {
    public record PharmacySettings(
            UUID pharmacyId,
            String name,
            String licenseNumber,
            String phoneNumber,
            String email,
            String address
    ) {
    }

    public record UserProfile(
            UUID userId,
            String fullName,
            String email,
            String phoneNumber,
            Role role
    ) {
    }
}
