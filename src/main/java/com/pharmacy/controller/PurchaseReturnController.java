package com.pharmacy.controller;

import com.pharmacy.dto.PurchaseReturnRequest;
import com.pharmacy.dto.PurchaseReturnResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.PurchaseReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Purchase Returns", description = "Return goods to supplier and adjust stock")
@RestController
@RequestMapping("/api/purchase-returns")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @Operation(summary = "List purchase returns", description = "Returns all purchase returns for the pharmacy.")
    @GetMapping
    public List<PurchaseReturnResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return purchaseReturnService.list(principal);
    }

    @Operation(summary = "Get purchase return", description = "Returns the full details of a single purchase return.")
    @GetMapping("/{id}")
    public PurchaseReturnResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return purchaseReturnService.get(principal, id);
    }

    @Operation(summary = "Create purchase return", description = "Records goods returned to a supplier and reduces stock quantities.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseReturnResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @Valid @RequestBody PurchaseReturnRequest request) {
        return purchaseReturnService.create(principal, request);
    }
}
