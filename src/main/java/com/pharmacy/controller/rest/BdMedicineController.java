package com.pharmacy.controller.rest;

import com.pharmacy.dto.BdMedicineResponse;
import com.pharmacy.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Master Medicine", description = "Global BD medicine catalogue operations")
@RestController
@RequestMapping("/api/master-medicines")
@RequiredArgsConstructor
public class BdMedicineController {

    private final InventoryService inventoryService;

    @Operation(summary = "Search master medicines", description = "Typeahead search for the global BD medicines.")
    @GetMapping("/search")
    public List<BdMedicineResponse> search(@RequestParam(name = "q", defaultValue = "") String q) {
        return inventoryService.searchMasterMedicines(q);
    }
}
