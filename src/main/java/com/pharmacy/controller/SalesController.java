package com.pharmacy.controller;

import com.pharmacy.dto.SalePaymentRequest;
import com.pharmacy.dto.SaleRequest;
import com.pharmacy.dto.SaleResponse;
import com.pharmacy.dto.SaleReturnRequest;
import com.pharmacy.dto.SaleReturnResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.SalesService;
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

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping
    public List<SaleResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return salesService.listSales(principal);
    }

    @GetMapping("/{id}")
    public SaleResponse get(@AuthenticationPrincipal AppUserPrincipal principal, @PathVariable UUID id) {
        return salesService.getSale(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                               @Valid @RequestBody SaleRequest request) {
        return salesService.createSale(principal, request);
    }

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse addPayment(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody SalePaymentRequest request) {
        return salesService.addPayment(principal, id, request);
    }

    @PostMapping("/{id}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    public SaleReturnResponse createReturn(@AuthenticationPrincipal AppUserPrincipal principal,
                                           @PathVariable UUID id,
                                           @Valid @RequestBody SaleReturnRequest request) {
        return salesService.createReturn(principal, id, request);
    }
}
