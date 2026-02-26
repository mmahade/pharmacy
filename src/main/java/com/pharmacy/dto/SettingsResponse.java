package com.pharmacy.dto;

import com.pharmacy.entity.Role;

public record SettingsResponse(
        PharmacySettings pharmacy,
        UserProfile user) {
    public record PharmacySettings(
            Long pharmacyId,
            String name,
            String licenseNumber,
            String phoneNumber,
            String email,
            String address) {
    }

    public record UserProfile(
            Long userId,
            String fullName,
            String email,
            String phoneNumber,
            Role role) {
    }
}
