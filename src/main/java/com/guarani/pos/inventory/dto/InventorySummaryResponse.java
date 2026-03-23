package com.guarani.pos.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventorySummaryResponse(
        BigDecimal inventoryValue,
        BigDecimal totalUnits,
        int activeProducts,
        int lowStockItems,
        long monthlyMovements,
        List<String> categories
) {
}
