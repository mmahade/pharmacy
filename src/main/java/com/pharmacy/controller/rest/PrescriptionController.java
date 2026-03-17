package com.pharmacy.controller.rest;

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
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Prescriptions", description = "Doctor prescription management and dispensing workflow")
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Operation(summary = "List prescriptions", description = "Returns paginated prescriptions for the pharmacy.")
    @GetMapping
    public Page<PrescriptionResponse> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        if (query != null && !query.isEmpty()) {
            return prescriptionService.searchPrescriptionsPaginated(principal, query, page, size);
        }
        return prescriptionService.listPrescriptionsPaginated(principal, page, size);
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
    public PrescriptionResponse complete(@AuthenticationPrincipal AppUserPrincipal principal, 
                                       @PathVariable Long id,
                                       @RequestBody com.pharmacy.dto.PrescriptionCompleteRequest request) {
        return prescriptionService.completePrescription(principal, id, request);
    }

    @Operation(summary = "Add payment to prescription", description = "Adds a payment for an existing sale transaction tied to a completed prescription.")
    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse addPayment(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @PathVariable Long id,
                                         @Valid @RequestBody com.pharmacy.dto.SalePaymentRequest request) {
        return prescriptionService.addPayment(principal, id, request);
    }
}
