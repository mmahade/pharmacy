package com.pharmacy.dto;

import com.pharmacy.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotNull Role role
) {
}
