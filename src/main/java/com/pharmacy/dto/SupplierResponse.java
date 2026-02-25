package com.pharmacy.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        Instant createdAt
) {
}
