package com.guarani.pos.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseReceiveItemRequest(
        @NotNull
        Long productId,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal quantity
) {
}
