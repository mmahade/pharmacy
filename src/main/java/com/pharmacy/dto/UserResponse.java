package com.pharmacy.dto;

import com.pharmacy.entity.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        Boolean active,
        Instant createdAt,
        Long pharmacyId) {
}
