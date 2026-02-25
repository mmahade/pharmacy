package com.pharmacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber
) {
}
