package com.pharmacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PharmacySettingsUpdateRequest(
        @NotBlank String name,
        @NotBlank String licenseNumber,
        @NotBlank String phoneNumber,
        @NotBlank @Email String email,
        @NotBlank String address
) {
}
