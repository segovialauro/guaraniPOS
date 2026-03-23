package com.guarani.pos.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InventoryAdjustRequest(
        @NotNull
        Long productId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal newStock,

        @NotBlank
        String reason
) {
}
