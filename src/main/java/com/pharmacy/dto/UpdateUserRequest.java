package com.pharmacy.dto;

import com.pharmacy.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank String fullName,
        String phoneNumber,
        @NotNull Role role,
        @NotNull Boolean active) {
}
