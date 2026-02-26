package com.pharmacy.controller;

import com.pharmacy.dto.SalePaymentRequest;
import com.pharmacy.dto.SaleRequest;
import com.pharmacy.dto.SaleResponse;
import com.pharmacy.dto.SaleReturnRequest;
import com.pharmacy.dto.SaleReturnResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SalesService;
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

@Tag(name = "Sales", description = "Point-of-sale operations, payment recording and sale returns")
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @Operation(summary = "List all sales", description = "Returns all sales for the authenticated user's pharmacy in reverse-chronological order.")
    @GetMapping
    public List<SaleResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return salesService.listSales(principal);
    }

    @Operation(summary = "Get sale by ID", description = "Returns the full details of a single sale, including line items and payments.")
    @GetMapping("/{id}")
    public SaleResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return salesService.getSale(principal, id);
    }

    @Operation(summary = "Create sale", description = "Records a new POS sale and decrements stock accordingly.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                               @Valid @RequestBody SaleRequest request) {
        return salesService.createSale(principal, request);
    }

    @Operation(summary = "Add payment", description = "Records an additional payment (cash, card, etc.) against an existing sale.")
    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse addPayment(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody SalePaymentRequest request) {
        return salesService.addPayment(principal, id, request);
    }

    @Operation(summary = "Create sale return", description = "Processes a partial or full return for a sale and restores stock.")
    @PostMapping("/{id}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleReturnResponse createReturn(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @PathVariable UUID id,
                                           @Valid @RequestBody SaleReturnRequest request) {
        return salesService.createReturn(principal, id, request);
    }
}
