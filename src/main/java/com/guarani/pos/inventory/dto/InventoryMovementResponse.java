package com.guarani.pos.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String productCode,
        String productName,
        String movementType,
        BigDecimal quantity,
        BigDecimal previousStock,
        BigDecimal newStock,
        String reason,
        String createdByName,
        LocalDateTime createdAt
) {
}
