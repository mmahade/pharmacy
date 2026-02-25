package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
        @NotBlank String name,
        String contactPerson,
        String email,
        String phone,
        String address
) {
}
