package com.pharmacy.controller;

import com.pharmacy.dto.PrescriptionRequest;
import com.pharmacy.dto.PrescriptionResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public List<PrescriptionResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return prescriptionService.listPrescriptions(principal);
    }

    @GetMapping("/{id}")
    public PrescriptionResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return prescriptionService.getPrescription(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrescriptionResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                       @Valid @RequestBody PrescriptionRequest request) {
        return prescriptionService.createPrescription(principal, request);
    }

    @PutMapping("/{id}/complete")
    public PrescriptionResponse complete(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return prescriptionService.completePrescription(principal, id);
    }
}
