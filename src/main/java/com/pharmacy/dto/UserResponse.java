package com.pharmacy.dto;

import com.pharmacy.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        Role role,
        Boolean active,
        Instant createdAt,
        UUID pharmacyId
) {
}
