package com.pharmacy.dto;

import java.util.UUID;

public record RegisterPharmacyResponse(
        UUID pharmacyId,
        String pharmacyName,
        UUID adminUserId,
        String adminEmail
) {
}
