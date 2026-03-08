package com.pharmacy.dto;

public record BdMedicineResponse(
        Long id,
        String name,
        String genericName,
        String category,
        String manufacturer,
        String dosageForm,
        String strength,
        String description
) {
}
