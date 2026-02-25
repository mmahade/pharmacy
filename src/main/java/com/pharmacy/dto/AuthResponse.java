package com.pharmacy.dto;

import com.pharmacy.entity.Role;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String fullName,
        String email,
        Role role,
        UUID pharmacyId,
        String pharmacyName
) {
}
