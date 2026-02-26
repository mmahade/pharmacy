package com.pharmacy.dto;

import com.pharmacy.entity.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        Role role,
        Long pharmacyId,
        String pharmacyName) {
}
