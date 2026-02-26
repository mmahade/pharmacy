package com.pharmacy.controller;

import com.pharmacy.dto.ExpiryAlertResponse;
import com.pharmacy.dto.MedicineRequest;
import com.pharmacy.dto.MedicineResponse;
import com.pharmacy.dto.StockBatchRequest;
import com.pharmacy.dto.StockBatchResponse;
import com.pharmacy.security.AppUserPrincipal;
import com.pharmacy.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Inventory", description = "Medicine catalogue and stock batch management")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

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
    @GetMapping("/medicines/{id}/batches")
    public List<StockBatchResponse> listBatches(@AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable UUID id) {
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
    @PostMapping("/medicines/{id}/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineResponse addBatch(@AuthenticationPrincipal AppUserPrincipal principal,
                                      @PathVariable UUID id,
                                      @Valid @RequestBody StockBatchRequest request) {
        return inventoryService.addBatch(principal, id, request);
    }
}
