package com.guarani.pos.inventory.dto;

import java.math.BigDecimal;

public record InventoryItemResponse(
        Long productId,
        String code,
        String name,
        String category,
        String unitMeasure,
        BigDecimal costPrice,
        BigDecimal salePrice,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        BigDecimal totalValue,
        String vatType,
        boolean lowStock
) {
}
