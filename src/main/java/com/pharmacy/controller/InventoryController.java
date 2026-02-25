package com.pharmacy.controller;

import com.pharmacy.dto.ExpiryAlertResponse;
import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.dto.StockBatchRequest;
import com.pharmacy.dto.StockBatchResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<MedicineResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return inventoryService.listMedicines(principal);
    }

    /**
     * Search medicines by name for prescription/sale entry (e.g. typeahead).
     * GET /api/inventory/search?q=para
     */
    @GetMapping("/search")
    public List<MedicineResponse> search(@AuthenticationPrincipal AppUserPrincipal principal,
                                         @RequestParam(name = "q", defaultValue = "") String q) {
        return inventoryService.searchMedicines(principal, q);
    }

    /**
     * Batches expiring within N days (e.g. 30, 60, 90). Default 30.
     */
    @GetMapping("/expiry-alerts")
    public List<ExpiryAlertResponse> expiryAlerts(@AuthenticationPrincipal AppUserPrincipal principal,
                                                    @RequestParam(name = "withinDays", defaultValue = "30") int withinDays) {
        return inventoryService.getExpiryAlerts(principal, withinDays);
    }

    /**
     * List all batches for a medicine (by expiry date).
     */
    @GetMapping("/medicines/{id}/batches")
    public List<StockBatchResponse> listBatches(@AuthenticationPrincipal AppUserPrincipal principal,
                                                @PathVariable UUID id) {
        return inventoryService.listBatchesForMedicine(principal, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @Valid @RequestBody MedicineRequest request) {
        return inventoryService.createMedicine(principal, request);
    }

    @PostMapping("/medicines/{id}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse addBatch(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @PathVariable UUID id,
                                      @Valid @RequestBody StockBatchRequest request) {
        return inventoryService.addBatch(principal, id, request);
    }
}
