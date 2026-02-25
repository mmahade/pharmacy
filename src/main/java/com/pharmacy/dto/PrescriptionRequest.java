package com.pharmacy.dto;

import com.pharmacy.entity.PrescriptionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PrescriptionRequest(
        @NotBlank String patientName,
        @NotBlank String doctorName,
        LocalDate prescriptionDate,
        @NotNull PrescriptionStatus status,
        @NotEmpty(message = "At least one medicine required") List<@Valid PrescriptionItemRequest> items
) {
}
