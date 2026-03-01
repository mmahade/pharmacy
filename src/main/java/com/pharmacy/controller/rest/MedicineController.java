package com.pharmacy.controller.rest;

import com.pharmacy.dto.*;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Medicine", description = "Medicine catalogue and stock batch management")
@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final InventoryService inventoryService;

    @Operation(summary = "List all medicines", description = "Returns all medicines registered under the authenticated user's pharmacy.")
    @GetMapping
    public List<MedicineResponse> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return inventoryService.listMedicines(principal);
    }

    /**
     * Search medicines by name for prescription/sale entry (e.g. typeahead).
     * GET /api/inventory/search?q=para
     */
    @Operation(summary = "Search medicines by name", description = "Typeahead search — returns medicines whose name contains the query string `q`.")
    @GetMapping("/search")
    public List<MedicineResponse> search(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "q", defaultValue = "") String q) {
        return inventoryService.searchMedicines(principal, q);
    }

    /**
     * Batches expiring within N days (e.g. 30, 60, 90). Default 30.
     */
    @Operation(summary = "Expiry alerts", description = "Returns stock batches expiring within the specified number of days (default 30).")
    @GetMapping("/expiry-alerts")
    public List<ExpiryAlertResponse> expiryAlerts(@AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam(name = "withinDays", defaultValue = "30") int withinDays) {
        return inventoryService.getExpiryAlerts(principal, withinDays);
    }

    /**
     * List all batches for a medicine (by expiry date).
     */
    @Operation(summary = "List batches for a medicine", description = "Returns all stock batches for the given medicine, ordered by expiry date.")
    @GetMapping("/{id}/batches")
    public List<StockBatchResponse> listBatches(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id) {
        return inventoryService.listBatchesForMedicine(principal, id);
    }

    @Operation(summary = "Create medicine", description = "Adds a new medicine to the pharmacy's catalogue.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse create(@AuthenticationPrincipal AppUserPrincipal principal,
            @Valid @RequestBody MedicineRequest request) {
        return inventoryService.createMedicine(principal, request);
    }

    @Operation(summary = "Add stock batch", description = "Adds a new stock batch (with quantities, batch number, and expiry) to an existing medicine.")
    @PostMapping("/{id}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse addBatch(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody StockBatchRequest request) {
        return inventoryService.addBatch(principal, id, request);
    }
}
