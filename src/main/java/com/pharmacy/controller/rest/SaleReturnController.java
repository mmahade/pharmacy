package com.pharmacy.controller.rest;

import com.pharmacy.dto.SaleReturnRequest;
import com.pharmacy.dto.SaleReturnResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SaleReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Sales Returns", description = "Process customer returns and restock inventory")
@RestController
@RequestMapping("/api/sale-returns")
@RequiredArgsConstructor
public class SaleReturnController {

    private final SaleReturnService saleReturnService;

    @Operation(summary = "List sales returns", description = "Returns all sales returns for the pharmacy.")
    @GetMapping
    public List<SaleReturnResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return saleReturnService.list(principal);
    }

    @Operation(summary = "Get sales return", description = "Returns the full details of a single sales return.")
    @GetMapping("/{id}")
    public SaleReturnResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable Long id) {
        return saleReturnService.get(principal, id);
    }

    @Operation(summary = "Create sales return", description = "Records a new customer return and restores stock quantities.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleReturnResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody SaleReturnRequest request) {
        return saleReturnService.create(principal, request);
    }
}
