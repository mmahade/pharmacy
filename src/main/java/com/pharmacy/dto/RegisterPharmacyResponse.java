package com.pharmacy.dto;

public record RegisterPharmacyResponse(
        Long pharmacyId,
        String pharmacyName,
        Long adminUserId,
        String adminEmail) {
}
