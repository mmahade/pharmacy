package com.pharmacy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPharmacyRequest(
        @NotBlank String pharmacyName,
        @NotBlank @Email String pharmacyEmail,
        @NotBlank String licenseNumber,
        @NotBlank String adminFullName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String adminPassword
) {
}
