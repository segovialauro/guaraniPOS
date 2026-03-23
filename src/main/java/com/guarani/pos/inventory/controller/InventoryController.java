package com.guarani.pos.inventory.controller;

import com.guarani.pos.inventory.dto.InventoryAdjustRequest;
import com.guarani.pos.inventory.dto.InventoryItemResponse;
import com.guarani.pos.inventory.dto.InventoryMovementResponse;
import com.guarani.pos.inventory.dto.InventorySummaryResponse;
import com.guarani.pos.inventory.service.InventoryService;
import com.guarani.pos.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/resumen")
    public InventorySummaryResponse getSummary() {
        return inventoryService.getSummary(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId()
        );
    }

    @GetMapping("/productos")
    public List<InventoryItemResponse> findProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean lowStockOnly
    ) {
        return inventoryService.findProducts(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                q,
                category,
                lowStockOnly
        );
    }

    @GetMapping("/movimientos")
    public List<InventoryMovementResponse> findMovements(@RequestParam(required = false) Long productId) {
        return inventoryService.findRecentMovements(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                productId
        );
    }

    @PostMapping("/ajustes")
    public InventoryMovementResponse adjustStock(@Valid @RequestBody InventoryAdjustRequest request) {
        return inventoryService.adjustStock(
                SecurityUtils.getCurrentCompanyId(),
                SecurityUtils.getCurrentUserId(),
                request
        );
    }
}
