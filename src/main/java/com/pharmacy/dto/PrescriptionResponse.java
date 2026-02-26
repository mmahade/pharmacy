package com.pharmacy.dto;

import com.pharmacy.entity.PrescriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        String prescriptionNumber,
        String patientName,
        String doctorName,
        LocalDate prescriptionDate,
        PrescriptionStatus status,
        BigDecimal totalAmount,
        List<PrescriptionItemResponse> items) {
}
