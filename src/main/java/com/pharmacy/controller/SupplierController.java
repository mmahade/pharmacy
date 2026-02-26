package com.pharmacy.controller;

import com.pharmacy.dto.SupplierRequest;
import com.pharmacy.dto.SupplierResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Suppliers", description = "Supplier directory management")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "List suppliers", description = "Returns all suppliers registered under the pharmacy.")
    @GetMapping
    public List<SupplierResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return supplierService.list(principal);
    }

    @Operation(summary = "Get supplier", description = "Returns the details of a single supplier.")
    @GetMapping("/{id}")
    public SupplierResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return supplierService.get(principal, id);
    }

    @Operation(summary = "Create supplier", description = "Adds a new supplier to the pharmacy's directory.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @Valid @RequestBody SupplierRequest request) {
        return supplierService.create(principal, request);
    }

    @Operation(summary = "Update supplier", description = "Updates the contact details of an existing supplier.")
    @PutMapping("/{id}")
    public SupplierResponse update(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody SupplierRequest request) {
        return supplierService.update(principal, id, request);
    }
}
