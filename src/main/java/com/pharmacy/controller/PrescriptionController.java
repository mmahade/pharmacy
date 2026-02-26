package com.pharmacy.controller;

import com.pharmacy.dto.PrescriptionRequest;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Prescriptions", description = "Doctor prescription management and dispensing workflow")
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "List prescriptions", description = "Returns all prescriptions for the pharmacy.")
    @GetMapping
    public List<PrescriptionResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return prescriptionService.listPrescriptions(principal);
    }

    @Operation(summary = "Get prescription", description = "Returns the full details of a single prescription.")
    @GetMapping("/{id}")
    public PrescriptionResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return prescriptionService.getPrescription(principal, id);
    }

    @Operation(summary = "Create prescription", description = "Records a new doctor prescription with medication line items.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                       @Valid @RequestBody PrescriptionRequest request) {
        return prescriptionService.createPrescription(principal, request);
    }

    @Operation(summary = "Complete prescription", description = "Marks a prescription as fully dispensed, deducting stock for each item.")
    @PutMapping("/{id}/complete")
    public PrescriptionResponse complete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return prescriptionService.completePrescription(principal, id);
    }
}
